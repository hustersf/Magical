package com.sofar.feature.ai.edge.chat.impl.detail

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.repository.ChatRepository
import com.sofar.core.ai.edge.data.repository.ModelsDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
  private val repository: ChatRepository,
) : ViewModel() {

  // 内部私有变量：负责动态修改状态
  private val _uiState = MutableStateFlow(ChatDetailUiState())
  val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

  // 观察离线数据库变动的工作句柄，换对话时用来强行掐断老的流
  private var dbObservationJob: Job? = null

  private var modelObservationJob: Job? = null

  private val _currentActiveModel = MutableStateFlow<Model?>(null)

  init {
    modelObservationJob = viewModelScope.launch {
      ModelsDataManager.activeModelFlow.collect { freshModel ->
        _currentActiveModel.value = freshModel
        if (freshModel == null) {
          _uiState.update { it.copy(errorMessage = "当前未在设置中激活任何本地大模型") }
        } else {
          prepareEngine(ModelsDataManager.appContext())
        }
      }
    }
  }

  /**
   * 🧱 业务一：单向挂载观察本地 Room 消息数据流
   * 🎯 大闭环关键：只要大模型在推理结束时往数据库塞数据，这根管道会瞬间捕捉，
   * 自动把最新列表同步进 _uiState.messages，从而触发 Activity 里的 ListAdapter 刷新！
   */
  fun startObservingSession(sessionId: String) {
    // 1. 如果之前已经挂载了别的对话，先安全断开，防止历史数据串门
    dbObservationJob?.cancel()

    // 2. 清空上一轮的残余报错信息
    _uiState.update { it.copy(errorMessage = null) }

    // 3. 强力挂载 Room 流监听
    dbObservationJob = viewModelScope.launch {
      val session = repository.getSessionById(sessionId)
      _uiState.update {
        it.copy(sessionTitle = session?.title ?: "")
      }

      repository.getMessagesBySession(sessionId)
        .collect { freshMessages ->
          _uiState.update { it.copy(messages = freshMessages) }
        }
    }
  }

  /**
   * 🧱 业务二：初始化并唤醒端侧 AI 引擎
   * 对应 Activity 的 initData 阶段，或者用户去 Tab 5 页面动态重载大模型时调用
   */
  fun prepareEngine(context: Context, agentSystemPrompt: String? = null) {
    val activeModel = _currentActiveModel.value ?: run {
      _uiState.update { it.copy(errorMessage = "当前未在设置中激活任何本地大模型") }
      return
    }

    // 1. 开启界面思考锁，置灰发送键，控制 UI 展示转圈 Loading 状态
    _uiState.update { it.copy(isEngineLoading = true, errorMessage = null) }

    // 2. 🚀 在主线程作用域中无忧启动协程，保持 UI 的极速响应
    viewModelScope.launch {
      val errorResult = repository.initializeModel(
        context = context,
        model = activeModel,
        agentSystemPrompt = agentSystemPrompt
      )

      // 4. 👍 子线程的 C++ 图编译彻底闭环，协程在主线程苏醒，直接在线性代码下方丝滑刷新 UI 状态
      _uiState.update {
        it.copy(
          isEngineLoading = false,
          isModelReady = errorResult.isEmpty(),
          errorMessage = errorResult.ifEmpty { null } // 若返回空字串代表启动成功，清除报错状态
        )
      }
    }
  }

  /**
   * 🧱 业务三：全能多模态消息发送（核心调度大脑）
   */
  fun performSendMessage(
    sessionId: String,
    inputTextFieldValue: String,
    sandboxedImagesPath: List<String> = listOf(),
    sandboxedAudioPath: String? = null
  ) {
    val userPrompt = inputTextFieldValue.trim()
    if (userPrompt.isEmpty() && sandboxedImagesPath.isEmpty() && sandboxedAudioPath == null) return

    val activeModel = _currentActiveModel.value ?: run {
      _uiState.update { it.copy(errorMessage = "当前未在设置中激活任何本地大模型") }
      return
    }

    if (_uiState.value.isAiResponding) return

    // 模型思考中
    _uiState.update {
      it.copy(
        isAiResponding = true,
        currentStreamingText = null,
        errorMessage = null
      )
    }

    viewModelScope.launch {
      // 在子线程中优雅解析多模态大文件，主线程获得绝对安全保护
      val (rawBitmaps, rawAudioBytes) = withContext(Dispatchers.IO) {
        val bitmaps = sandboxedImagesPath.mapNotNull { path ->
          val file = File(path)
          if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }
        val audioBytes =
          sandboxedAudioPath?.let { path -> File(path).let { if (it.exists()) it.readBytes() else null } }
        bitmaps to audioBytes
      }

      // 🌊 核心流式对接：调度底层的 LiteRT 推理管道
      repository.sendMessageStream(
        sessionId = sessionId, model = activeModel, input = userPrompt,
        images = rawBitmaps, audioClips = rawAudioBytes?.let { listOf(it) } ?: listOf(),
        inputImagesPath = sandboxedImagesPath, inputAudioPath = sandboxedAudioPath
      )
        .catch { exception ->
          // 遇错截流，将专属的响应状态拉回 false
          _uiState.update {
            it.copy(
              isAiResponding = false,
              currentStreamingText = null,
              errorMessage = exception.message
            )
          }
        }
        .onCompletion {
          // 整个大模型流式喷射谢幕，重置响应指标，把画面权完美交还给 Room
          val session = repository.getSessionById(sessionId)
          _uiState.update {
            it.copy(
              sessionTitle = session?.title ?: "",
              isAiResponding = false,
              currentStreamingText = null
            )
          }
        }
        .collect { accumulatedText ->
          // 🚀【修复核心：打字机流转】：高频把全量拼好的词通过单状态送往 Activity 看板
          // 💡 注意：在此期间，isAiResponding 依旧保持为 true 锁存，以驱动前台正方形打断按钮的常驻
          _uiState.update {
            it.copy(
              currentStreamingText = accumulatedText
            )
          }
        }
    }
  }

  /**
   * 🧱 业务四：一键掐断大模型输出
   */
  fun stopModelResponse() {
    _currentActiveModel.value?.let { repository.stopModelResponse(it) }
    _uiState.update {
      it.copy(
        isAiResponding = false,
        currentStreamingText = null
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    dbObservationJob?.cancel()
    modelObservationJob?.cancel()
  }
}
