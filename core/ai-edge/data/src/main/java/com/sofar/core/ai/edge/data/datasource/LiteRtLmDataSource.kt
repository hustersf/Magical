package com.sofar.core.ai.edge.data.datasource

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ToolProvider
import com.sofar.core.ai.edge.data.entity.models.Model

/**
 * 结果回调别名
 * @param partialResult 当前单次返回的文本片段（流式输出）
 * @param done 是否输出完毕（true 表示结束）
 * @param partialThinkingResult 当前单次返回的思考过程片段（部分模型支持 Reasoning 思考链流式输出）
 */
typealias ResultListener = (partialResult: String, done: Boolean, partialThinkingResult: String?) -> Unit

/**
 * 清理资源完成后的回调别名
 */
typealias CleanUpListener = () -> Unit

/**
 * LiteRT 本地大语言模型数据源接口
 * 定义了在 Android 客户端端侧运行 LLM 的标准行为
 */
interface LiteRtLmDataSource {

  /**
   * 初始化大模型及其运行环境
   *
   * @param context Android 上下文，用于读取资产或本地存储的模型文件
   * @param model 模型实体对象，包含模型路径、配置等信息
   * @param supportImage 是否启用图片多模态支持
   * @param supportAudio 是否启用音频多模态支持
   * @param onDone 初始化完成后的回调通知（返回初始化状态或信息）
   * @param systemInstruction 系统指令/提示词，用于设定 AI 的角色、语气或行为准则
   * @param tools 提供给模型的外部工具/函数列表（用于 Function Calling 功能）
   * @param enableConversationConstrainedDecoding 是否开启对话约束解码（限制或规范模型的生成范围）
   * @param coroutineScope 用于执行异步初始化操作的协程作用域
   */
  fun initialize(
    context: Context,
    model: Model,
    supportImage: Boolean,
    supportAudio: Boolean,
    onDone: (String) -> Unit,
    systemInstruction: Contents? = null,
    tools: List<ToolProvider> = listOf(),
    enableConversationConstrainedDecoding: Boolean = false,
  )

  /**
   * 重置当前对话状态（清除历史上下文，开始新一轮对话）
   * 参数配置通常与 initialize 保持一致，以便重新配置模型上下文
   */
  fun resetConversation(
    model: Model,
    supportImage: Boolean = false,
    supportAudio: Boolean = false,
    systemInstruction: Contents? = null,
    tools: List<ToolProvider> = listOf(),
    enableConversationConstrainedDecoding: Boolean = false,
  )

  /**
   * 执行 AI 推理（向模型发送请求并获取响应）
   *
   * @param model 执行推理的目标模型
   * @param input 用户输入的文本问题
   * @param resultListener 流式结果监听器，用于接收 AI 实时生成的文本
   * @param cleanUpListener 单词推理完成后触发的清理回调
   * @param onError 发生错误时的回调，返回错误信息
   * @param images 可选参数：输入的图片列表（多模态视觉输入）
   * @param audioClips 可选参数：输入的音频字节数组列表（多模态语音输入）
   * @param extraContext 可选参数：额外的上下文 KV 键值对，用于传递业务自定义参数
   */
  fun runInference(
    model: Model,
    input: String,
    resultListener: ResultListener,
    cleanUpListener: CleanUpListener,
    onError: (message: String) -> Unit = {},
    images: List<Bitmap> = listOf(),
    audioClips: List<ByteArray> = listOf(),
    extraContext: Map<String, String>? = null,
  )

  /**
   * 强行停止当前的 AI 响应（例如用户在 AI 流式生成文本时点击了“停止”按钮）
   */
  fun stopResponse(model: Model)

  /**
   * 彻底销毁模型实例并释放占用的内存、GPU 或 NPU 资源
   *
   * @param model 需要注销的模型
   * @param onDone 资源释放完毕后的回调
   */
  fun cleanUp(model: Model, onDone: () -> Unit)
}
