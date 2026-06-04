package com.sofar.core.ai.edge.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ToolProvider
import com.sofar.core.ai.edge.data.datasource.LiteRtLmDataSource
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageRole
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageType
import com.sofar.core.ai.edge.data.entity.chat.ChatPriority
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.database.dao.MessageDao
import com.sofar.core.ai.edge.database.dao.SessionDao
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ai.edge.database.entity.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
  private val sessionDao: SessionDao,
  private val messageDao: MessageDao,
  private val dataSource: LiteRtLmDataSource
) {

  private var lastInitializedModelName: String? = null

  // =================================================================================
  // 🧱 第一部分：会话列表（Sessions）核心管理逻辑
  // =================================================================================

  /**
   * 提供给首页的真实数据流
   * 🎯 使用 onStart 拦截：在首页刚开始监听数据的一瞬间，以最高的优先级确保置顶框的存在
   */
  fun getAllSessionsWithWorkspace(): Flow<List<SessionEntity>> {
    return sessionDao.getAllSessions()
      .onStart { ensureWorkspaceExist() } // 核心保底：无感检查并初始化置顶框
      .flowOn(Dispatchers.IO) // 强制切到 I/O 线程，杜绝主线程卡顿
  }

  /**
   * 确保数据库里至少存在一个置顶的工作区
   */
  private suspend fun ensureWorkspaceExist() {
    // 检查当前数据库中是否已经有置顶的常驻对话框
    val hasWorkspace = sessionDao.getWorkspaceCount(ChatPriority.WORKSPACE) > 0
    if (!hasWorkspace) {
      // 如果是初次冷启动（用户刚下载 App），原地在数据库中正式持久化一条置顶主会话
      val defaultWorkspace = SessionEntity(
        id = UUID.randomUUID().toString(),
        title = "阿福",
        agentId = null,
        type = ChatSessionType.TEXT,
        updatedAt = System.currentTimeMillis(),
        priority = ChatPriority.WORKSPACE
      )
      sessionDao.insertSession(defaultWorkspace)
    }
  }

  suspend fun getSessionById(sessionId: String): SessionEntity? {
    return sessionDao.getSessionById(sessionId)
  }

  /**
   * 单向挂载观察本地 Room 消息数据流
   * 🎯 保证上层拿到的 Flow 天生就运行在安全的 I/O 线程池中
   */
  fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>> {
    return messageDao.getMessagesBySession(sessionId)
      .flowOn(Dispatchers.IO)
  }


  // =================================================================================
  // 🧱 第二部分：端侧大模型（LiteRT-LM）多模态高频 Stream 覆盖写落盘
  // =================================================================================

  /**
   * 初始化底层的擎配置
   */
  suspend fun initializeModel(
    context: Context,
    model: Model,
    agentSystemPrompt: String? = null,
    tools: List<ToolProvider> = listOf()
  ): String = withContext(Dispatchers.IO) { // ⚡【核心保护】：强制切到子线程（I/O 线程池）

    if (lastInitializedModelName == model.name) {
      Log.d("ChatRepository", "模型 '${model.name}' 已经初始化过，跳过本次初始化")
      return@withContext ""
    }

    // 1. 防内存泄漏防御：强行将上下文转换为长生命周期的 Application 级别 [^5]
    val appContext = context.applicationContext

    // 2. 此时这一步解析提示词的操作，也已经安全地运行在子线程了
    val systemInstruction = agentSystemPrompt?.let { Contents.of(it) }

    // 3. 异步转挂起胶水：在子线程环境中，静静等待底层的异步回调
    val errorMessage = suspendCancellableCoroutine { continuation ->
      dataSource.initialize(
        context = appContext,
        model = model,
        supportImage = model.llmSupportImage,
        supportAudio = model.llmSupportAudio,
        systemInstruction = systemInstruction,
        tools = tools,
        onDone = { errorMessage ->
          // 4. 当底层 C++ 引擎在后台彻底初始化或图编译（Graph Compilation）完毕后，触发此回调
          if (continuation.isActive) {
            // 回传报错信息（或空字串），宣告协程苏醒
            continuation.resume(errorMessage) { cause, _, _ ->
              Log.d(
                "ChatRepository",
                "初始化结果回传时协程遭强掐熔断，原因: ${cause.message}"
              )
            }
          }
        }
      )
    }

    lastInitializedModelName = if (errorMessage.isEmpty()) {
      model.name
    } else {
      // 如果初始化失败了，清空标记，确保下次进来能允许重试
      null
    }

    return@withContext errorMessage
  }

  /**
   * 发送多模态消息并获取流式响应
   * 最下方 .flowOn 音接管 IO 线程，内部 launch 无需再指定线程
   */
  fun sendMessageStream(
    sessionId: String,
    model: Model,
    input: String,
    images: List<Bitmap> = listOf(),
    audioClips: List<ByteArray> = listOf(),
    inputImagesPath: List<String> = listOf(),
    inputAudioPath: String? = null
  ): Flow<String> = callbackFlow {

    val userMsgId = UUID.randomUUID().toString()
    val modelMessageId = UUID.randomUUID().toString()
    val responseAccumulator = StringBuilder()

    // 背压通道：容量为1，高频蹦字时只留最新完整文本
    val dbUpdateChannel =
      Channel<String>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    // 1. 离线优先流水线：严格保证“初始化会话 -> 插入用户消息 -> 插入AI占位消息”的拓扑顺序，杜绝外键崩溃
    val initJob = launch {
      val currentMessageContentType = when {
        inputImagesPath.isNotEmpty() -> ChatMessageType.IMAGE
        inputAudioPath != null -> ChatMessageType.AUDIO
        else -> ChatMessageType.TEXT
      }

      var currentSession = sessionDao.getSessionById(sessionId)
      if (currentSession == null) {
        currentSession = SessionEntity(
          id = sessionId,
          title = "",
          agentId = null,
          type = ChatSessionType.TEXT,
          updatedAt = System.currentTimeMillis(),
          priority = ChatPriority.NORMAL
        )
        sessionDao.insertSession(currentSession)
      }

      // 插入用户消息
      messageDao.insertMessage(
        MessageEntity(
          id = userMsgId,
          sessionId = sessionId,
          role = ChatMessageRole.USER,
          contentType = currentMessageContentType,
          textContent = input.ifEmpty { null },
          filePath = if (currentMessageContentType == ChatMessageType.IMAGE) inputImagesPath.joinToString(
            ","
          ) else inputAudioPath,
          createdAt = System.currentTimeMillis()
        )
      )

      // 紧接着插入 AI 空白占位消息，确保严格的时序和外键安全
      messageDao.insertMessage(
        MessageEntity(
          id = modelMessageId,
          sessionId = sessionId,
          role = ChatMessageRole.ASSISTANT,
          contentType = ChatMessageType.TEXT,
          textContent = "",
          filePath = null,
          createdAt = System.currentTimeMillis() + 10 // 确保时序在用户消息之后
        )
      )

      // 更新会话置顶
      sessionDao.updateSessionTimeAndTitle(
        sessionId,
        System.currentTimeMillis(),
        currentSession.title
      )
    }

    // 2. 核心增量落盘流水线
    val dbJob = launch {
      dbUpdateChannel.consumeAsFlow()
        .debounce(100) // 限制每 100ms 最多写一次数据库
        .collect { throttledText ->
          // 确保占位消息已经创建完毕后，才进行打字机式更新
          initJob.join()
          messageDao.insertMessage(
            MessageEntity(
              id = modelMessageId,
              sessionId = sessionId,
              role = ChatMessageRole.ASSISTANT,
              contentType = ChatMessageType.TEXT,
              textContent = throttledText,
              filePath = null,
              createdAt = System.currentTimeMillis()
            )
          )
        }
    }

    // 3. 调度底层引擎执行端侧推理
    dataSource.runInference(
      model = model,
      input = input,
      images = images,
      audioClips = audioClips,
      resultListener = { partialResult, done, _ ->
        if (!done) {
          // 情况 A：大模型流式吐字中
          responseAccumulator.append(partialResult)
          val currentFullText = responseAccumulator.toString()

          trySend(currentFullText)      // 瞬间打入 Flow 管道，保障 UI 视觉极速流畅
          dbUpdateChannel.trySend(currentFullText) // 扔进限流通道，交由 debounce 合并写盘
        } else {
          // 情况 B：推理彻底终结
          val finalAnswer = responseAccumulator.toString()

          launch {
            try {
              dbUpdateChannel.close() // 安全关闭通道
              dbJob.join()            // 优雅等待未完结的 debounce 事务结束，绝不使用 cancel()

              // 写入最终完整答案
              initJob.join()
              messageDao.insertMessage(
                MessageEntity(
                  id = modelMessageId,
                  sessionId = sessionId,
                  role = ChatMessageRole.ASSISTANT,
                  contentType = ChatMessageType.TEXT,
                  textContent = finalAnswer,
                  filePath = null,
                  createdAt = System.currentTimeMillis()
                )
              )

              // 智能生成摘要标题
              val currentSession = sessionDao.getSessionById(sessionId)
              var autoSummaryTitle = currentSession?.title ?: ""

              if (finalAnswer.isNotEmpty() && autoSummaryTitle.isEmpty()) {
                val cleanInput = input.trim().replace("\n", " ")
                autoSummaryTitle = if (cleanInput.length > 15) {
                  cleanInput.take(15) + "..."
                } else {
                  cleanInput
                }
              }

              // AI 彻底说完后，用大模型最终答案作为最新的 lastMessage 覆写数据库
              val aiPreviewText = finalAnswer.trim().replace("\n", " ")
              sessionDao.updateSessionPreviewAndTitle(
                sessionId,
                System.currentTimeMillis(),
                autoSummaryTitle,
                aiPreviewText
              )
            } finally {
              close() // 优雅宣告当前 Flow 数据流完结
            }
          }
        }
      },
      cleanUpListener = {
        Log.d("ChatRepository", "单次推理结束，底层芯片缓存清理成功。")
      },
      onError = { errorMessage ->
        // 发生异常时同样使用守护线程去清理和销毁，确保状态正确
        launch {
          dbUpdateChannel.close()
          dbJob.cancel()
          initJob.cancel()
          messageDao.deleteMessageById(modelMessageId)
          close(Exception(errorMessage)) // 异常上抛
        }
      }
    )

    // 4. 熔断保护：上层 UI 取消监听时强掐底层 C++ 推理控温
    awaitClose {
      dataSource.stopResponse(model)
    }
  }.flowOn(Dispatchers.IO)

  /**
   * 对应用户在详情页聊天时，点击“正方形按钮”强行打断大模型继续输出
   */
  fun stopModelResponse(model: Model) {
    dataSource.stopResponse(model = model)
  }


  /**
   * 彻底注销并归还大模型在手机里持有的物理运行内存空间
   */
  fun releaseModelInstance(model: Model, onDone: () -> Unit) {
    dataSource.cleanUp(model = model, onDone = onDone)
  }
}