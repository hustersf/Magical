package com.sofar.core.ai.edge.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ToolProvider
import com.sofar.core.ai.edge.data.entity.llm.Accelerator
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_MAX_TOKEN
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TEMPERATURE
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TOPK
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TOPP
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_VISION_ACCELERATOR
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.util.cleanUpMediapipeTaskErrorMessage
import java.io.ByteArrayOutputStream
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val TAG = "DefaultLiteRtLmDataSource"

data class LlmModelInstance(val engine: Engine, var conversation: Conversation)

class DefaultLiteRtLmDataSource @Inject constructor() : LiteRtLmDataSource {

  // 用来暂存每一个模型推理结束后的内存清理任务（打扫战场用）
  private val cleanUpListeners: MutableMap<String, CleanUpListener> = mutableMapOf()

  // ⚡【核心重点】：用来装手机里真正运行的大模型实例池（每一个模型名字映射一个真实的C++引擎和会话）
  private val modelInstances = ConcurrentHashMap<String, LlmModelInstance>()

  @OptIn(ExperimentalApi::class)
  override fun initialize(
    context: Context,
    model: Model,
    supportImage: Boolean,
    supportAudio: Boolean,
    onDone: (String) -> Unit,
    systemInstruction: Contents?,
    initialMessages: List<Message>,
    tools: List<ToolProvider>,
    enableConversationConstrainedDecoding: Boolean,
  ) {
    // 1. 读取并校验上下文窗口容量限制（Token 上限），优先使用业务模型定义的阈值，若无则使用全局系统默认值
    val maxTokens = model.llmMaxToken

    // 2. 加载大模型采样核心超参数：TopK（候选词截断）、TopP（核采样概率分布上限）和 Temperature（文本生成随机度）
    val topK = model.llmTopK
    val topP = model.llmTopP
    val temperature = model.llmTemperature

    // 3. 配置核心推断加速器以及多模态视觉芯片后端，默认使用硬件图形加速芯片（GPU）
    val accelerator = model.accelerators.getOrNull(0)?.label ?: Accelerator.GPU.label
    val visionAccelerator = model.visionAccelerator.label

    // 4. 根据多模态视觉硬件标签，实例化对应的执行后端（CPU / GPU / NPU）。若选用 NPU 则需注入当前 App 的动态链接库物理路径
    val visionBackend = when (visionAccelerator) {
      Accelerator.CPU.label -> Backend.CPU()
      Accelerator.GPU.label -> Backend.GPU()
      Accelerator.NPU.label -> Backend.NPU(nativeLibraryDir = context.applicationInfo.nativeLibraryDir)
      else -> Backend.GPU()
    }
    val shouldEnableImage = supportImage
    val shouldEnableAudio = supportAudio

    // 5. 实例化文本主推断流水线的底层硬件执行后端，用于承载大模型的核心文本生成任务
    val preferredBackend = when (accelerator) {
      Accelerator.CPU.label -> Backend.CPU()
      Accelerator.GPU.label -> Backend.GPU()
      Accelerator.NPU.label -> Backend.NPU(nativeLibraryDir = context.applicationInfo.nativeLibraryDir)
      else -> Backend.CPU()
    }

    // 6. 获取模型文件在当前系统沙盒环境下的绝对文件物理路径
    val modelPath = model.getPath(context = context)

    // 7. 组装端侧推理引擎核心配置类，将各模态后端芯片（文本/视觉/音频）、文件映射路径及上下文滑动窗口尺寸进行统一封装
    val engineConfig = EngineConfig(
      modelPath = modelPath,
      backend = preferredBackend,
      visionBackend = if (shouldEnableImage) visionBackend else null, // 仅在启用图像多模态时挂载视觉加速器
      audioBackend = if (shouldEnableAudio) Backend.CPU() else null,   // 仅在启用音频多模态时挂载音频处理后端
      maxNumTokens = maxTokens,
      // 针对测试开发环境（如本地 adb 调试路径），挂载外部编译缓存目录以加快模型二次加载的图编译速度
      cacheDir = if (modelPath.startsWith("/data/local/tmp")) context.getExternalFilesDir(null)?.absolutePath else null,
    )

    try {
      // 8. 【防 OOM 内存防御机制】：移动端物理运存极度受限，在实例化新模型前，必须显式掐断并卸载内存中同名的旧缓存单例
      // 释放旧有会话上下文并关闭底层 C++ 推理引擎实体，斩断全局 Map 引用，防止多实例并存引发进程遭系统强杀
      modelInstances[model.name]?.conversation?.close()
      modelInstances[model.name]?.engine?.close()
      modelInstances.remove(model.name)

      Log.d(TAG, "正在初始化模型: '${model.name}'")
      // 9. 正式构建 Google LiteRT 推理引擎实例，并将模型二进制权重文件反序列化至系统运存中进行图结构编译
      val engine = Engine(engineConfig)
      engine.initialize()

      // 10. 临时修改全局实验性标记位，用于开启或关闭对话约束解码（例如配合 Schema 强行限制 LLM 输出 JSON 结构数据）
      ExperimentalFlags.enableConversationConstrainedDecoding =
        enableConversationConstrainedDecoding

      // 11. 依托就绪的底层引擎创建对话多轮状态跟踪器（Conversation），自动维护多轮交互中的历史上下文缓存
      val conversation = engine.createConversation(
        ConversationConfig(
          // ❗【硬件兼容性限制】：当文本主后端选用本地 NPU 芯片时，采样器配置 SamplerConfig 必须强制设为 null
          // 因为现阶段端侧 NPU 底层驱动无法动态支持随机采样的矩阵计算，强行配置会导致 C++ 运行时直接硬崩溃
          samplerConfig = if (preferredBackend is Backend.NPU) {
            null
          } else {
            // 常规 CPU / GPU 芯片正常注入控制大模型发散创造力的采样概率参数
            SamplerConfig(
              topK = topK,
              topP = topP.toDouble(),
              temperature = temperature.toDouble()
            )
          },
          systemInstruction = systemInstruction, // 注入系统预设提示词（System Prompt），建立智能体的角色边界与行为准则
          initialMessages = initialMessages, // 注入历史消息上下文
          tools = tools, // 注册提供给大模型的本地可调用函数或工具列表，激活端侧 Function Calling 插件生态
        )
      )
      // 12. 会话上下文安全建立完毕后，立即重置约束解码全局标记位，避免影响后续其他引擎的常规初始化逻辑
      ExperimentalFlags.enableConversationConstrainedDecoding = false

      // 13. 将构建成功的全套大模型运行时状态机托管在数据源内部的并发私有实例池中，以供后续执行流式推断
      modelInstances[model.name] =
        LlmModelInstance(engine = engine, conversation = conversation)
    } catch (e: Exception) {
      // 14. 捕获模型文件缺失、损坏或底层编译翻车等运行期异常，通过清洗函数规范化来自 C++ 层的原生报错堆栈后向上回调
      onDone(cleanUpMediapipeTaskErrorMessage(e.message ?: "Unknown error"))
      Log.d(TAG, "初始化模型失败: '${model.name}', 原因: ${e.message}")
      return
    }
    // 15. 初始化流程圆满结束，回传空字符串向上层通知当前模型已成功常驻内存并就绪
    Log.d(TAG, "初始化模型成功: '${model.name}'")
    onDone("")
  }

  @OptIn(ExperimentalApi::class)
  override fun resetConversation(
    model: Model,
    supportImage: Boolean,
    supportAudio: Boolean,
    systemInstruction: Contents?,
    initialMessages: List<Message>,
    tools: List<ToolProvider>,
    enableConversationConstrainedDecoding: Boolean
  ) {
    try {
      Log.d(TAG, "正在重置模型对话上下文: '${model.name}'")

      // 1. 进行可用性检查，若目标模型尚未在运行时内存池中实例化，则直接拦截并终止重置逻辑
      val instance = modelInstances[model.name] ?: return

      // 2. 【核心内存防漏】：显式关闭并销毁当前的会话管理对象，清空 C++ 底层缓存的所有多轮对话历史 Token 数据
      instance.conversation.close()

      // 3. 复用当前已常驻内存的底层引擎实例，避免重新执行数 GB 大模型文件的反序列化与图编译耗时
      val engine = instance.engine
      val topK = model.llmTopK
      val topP = model.llmTopP
      val temperature = model.llmTemperature
      val accelerator = model.accelerators.getOrNull(0)?.label ?: Accelerator.GPU.label

      // 4. 临时配置约束解码的全局实验性标记位，以便在重新构建会话时应用最新的 Schema 强约束规则
      ExperimentalFlags.enableConversationConstrainedDecoding =
        enableConversationConstrainedDecoding

      // 5. 基于原有硬件引擎重新构建全新的多轮对话状态跟踪器（Conversation），实现上下文窗口的彻底清空与初始化
      val newConversation = engine.createConversation(
        ConversationConfig(
          // ❗【硬件架构约束】：若当前执行后端为 NPU（神经网络处理器），则采样器配置 SamplerConfig 必须设为 null
          // 维持移动端 NPU 驱动层无法在运行时进行动态随机采样矩阵计算的兼容性底线，防止 C++ 抛出硬崩溃
          samplerConfig = if (accelerator == Accelerator.NPU.label) {
            null
          } else {
            // 常规 CPU / GPU 加速后端则正常注入控制生成文本随机度的采样概率参数
            SamplerConfig(
              topK = topK,
              topP = topP.toDouble(),
              temperature = temperature.toDouble()
            )
          },
          systemInstruction = systemInstruction, // 重新注入最新的系统预设提示词（System Prompt），实现智能体人设的动态覆写
          initialMessages = initialMessages, // 注入历史消息上下文
          tools = tools, // 重新绑定或更新当前会话允许调用的端侧 Function Calling 工具链列表
        )
      )
      // 6. 会话生命周期状态重置完毕后，立刻将约束解码的全局实验性标记位恢复默认值，防止状态污染
      ExperimentalFlags.enableConversationConstrainedDecoding = false

      // 7. 将新生成的空上下文会话状态机热替换至托管池的实例中，完成对话周期的安全无感重置
      instance.conversation = newConversation
      Log.d(TAG, "模型上下文重置完毕")
    } catch (e: Exception) {
      Log.d(TAG, "重置会话失败", e)
    }
  }

  override fun runInference(
    model: Model,
    input: String,
    resultListener: ResultListener,
    cleanUpListener: CleanUpListener,
    onError: (message: String) -> Unit,
    images: List<Bitmap>,
    audioClips: List<ByteArray>,
    extraContext: Map<String, String>?
  ) {
    // 1. 进行可用性检查，若目标模型尚未在运行时内存池中实例化，则直接拦截并终止推断逻辑
    val instance = modelInstances[model.name]
    if (instance == null) {
      onError("大模型尚未在运行内存中就绪，请先初始化。")
      return
    }

    // 2. 注册当前会话生命周期结束时的清理回调，确保执行完异步推断后能够触发上层的资源回收机制
    if (!cleanUpListeners.containsKey(model.name)) {
      cleanUpListeners[model.name] = cleanUpListener
    }

    // 3. 获取当前活跃的模型会话句柄（Conversation），该对象持有当前对话的上下文滑动窗口状态
    val conversation = instance.conversation

    // 4. 构建多模态数据输入队列，用于按特定序列封装并对齐不同维度的多模态特征数据
    val contents = mutableListOf<Content>()

    // A. 遍历图像输入源，利用图像压缩工具将 Bitmap 转换为 C++ 推理后端识别的原始二进制字节数组并入队
    for (image in images) {
      contents.add(Content.ImageBytes(image.toPngByteArray()))
    }

    // B. 遍历音频输入源，将原生语音字节流封装为音频内容特征并直接追加至多模态输入队列
    for (audioClip in audioClips) {
      contents.add(Content.AudioBytes(audioClip))
    }

    // C. ❗【多模态序列编排约束】：校验并提取用户输入的文本指令。
    // 根据 Transformer 架构底层的注意力机制（Attention Mechanism）特质，文本指令必须置于多模态序列的尾部，
    // 以确保模型在自回归生成时能够聚焦于当下的即时提问，避免引发上下文理解偏差或幻觉现象。
    if (input.trim().isNotEmpty()) {
      contents.add(Content.Text(input))
    }

    // 5. 调用 Google AI Edge SDK 的非阻塞异步推断流水线，将组装就绪的多模态数据分发至底层硬件执行引擎
    conversation.sendMessageAsync(
      Contents.of(contents),
      object : MessageCallback {

        // 核心流式回调：当底层 C++ 引擎每生成一个新的 Token 或文本片段时触发，用于上层实时渲染 UI
        override fun onMessage(message: Message) {
          // 通过回调向上层管道传递当前片段，参数二标识“非结束状态”，使前端 UI 气泡保持动态生长效果
          resultListener(message.toString(), false, null)
        }

        // 推断终止回调：当模型完成整句自回归生成、命中终止符（EOS）或达到最大步数时触发
        override fun onDone() {
          // 向上传递终止状态位（done = true），用以触发上层业务（如 Room 数据库）进行多轮对话存盘写入
          resultListener("", true, null)
        }

        // 运行时异常回调：当底层出现芯片算子编译错误、运存抖动或执行任务被外部强制打断时触发
        override fun onError(throwable: Throwable) {
          // 拦截协同取消异常（通常由用户手动执行 stopResponse 打断矩阵计算所引发）
          if (throwable is CancellationException) {
            // 将主动打断视为当前计算周期的安全落地行为，记录日志并向上传递结束标记以优雅关闭当前气泡
            Log.i(TAG, "用户主动取消了本次大模型推理。")
            resultListener("", true, null)
          } else {
            // 属于不可抗力的运行期硬件层或底层引擎致命报错，记录异常堆栈并通过通用错误管道上报至 UI 展现层
            Log.e(TAG, "大模型底层计算报错", throwable)
            onError("模型计算出错: ${throwable.message}")
          }
        }
      },
    )
  }


  override fun stopResponse(model: Model) {
    // 进行可用性检查，若当前模型实例未在内存池中运行，则不执行任何操作
    val instance = modelInstances[model.name] ?: return
    // 显式调用底层会话控制器的取消方法，强行中断当前 C++ 推理引擎正在进行的自回归矩阵运算，并截断流式输出
    instance.conversation.cancelProcess()
  }

  override fun cleanUp(model: Model, onDone: () -> Unit) {
    // 校验目标模型是否存在于当前的运行时内存池中，避免对未初始化的实例执行无效清理
    if (!modelInstances.containsKey(model.name)) {
      return
    }

    val instance = modelInstances[model.name]!!

    // 【生命周期销毁机制】：由于端侧大模型对物理运行内存（RAM）的开销极大，必须执行深度显式释放
    // 释放 A：尝试关闭当前的会话上下文，彻底抹除底层 C++ 运行时持有的临时 Token 上下文历史
    try {
      instance.conversation.close()
    } catch (e: Exception) {
      Log.e(TAG, "关闭会话失败: ${e.message}")
    }

    // 释放 B：尝试关闭模型推理核心引擎（Engine），将加载到 NPU/GPU/RAM 的数 GB 静态图权重安全退还给手机操作系统
    try {
      instance.engine.close()
    } catch (e: Exception) {
      Log.e(TAG, "释放引擎失败: ${e.message}")
    }

    // 解除当前模型在全局并发映射表中登记的生命周期清理监听器（CleanUpListener），并触发其回调
    val onCleanUp = cleanUpListeners.remove(model.name)
    if (onCleanUp != null) {
      onCleanUp()
    }

    // 从内存常驻池 Map 中彻底移除该模型的运行时对象，切断所有强引用链，以便触发 JVM 层的垃圾回收（GC）
    modelInstances.remove(model.name)

    // 同步激活异步完成回调通知上层框架，表明该端侧模型持有的硬件资源与系统运存已安全打扫干净
    onDone()
    Log.d(TAG, "内存完全退还完毕。")
  }

  /**
   * 【私有工具函数】：多模态图像编码转换器
   * 利用系统底层压缩矩阵，将系统运行内存中的 Bitmap 位图对象转化为无损的 PNG 二进制字节数组（ByteArray）。
   * 这一转换步骤是端侧多模态模型进行视觉图特征对齐（Vision Feature Alignment）和输入的硬性前置要求。
   */
  private fun Bitmap.toPngByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    // 以 100% 最高无损编码质量将位图矩阵序列化压缩至输出流中
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    // 抽取并返回底层的二进制字节数组
    return stream.toByteArray()
  }
}