package com.sofar.core.speech.internal.architecture

import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError

/**
 * 显式有限状态机：speech recognition 全生命周期的状态集合。
 *
 * 状态转移路径（单向保证）：
 *   Idle → Preparing → [ReadyToUse | ModelPreparationFailed] → Listening →
 *     [Completing | RecognitionFailed] → Idle
 */
internal sealed class RecognitionState(val name: String) {
  override fun toString(): String = name

  /**
   * 初始态：未初始化、未开始准备。
   */
  object Idle : RecognitionState("Idle")

  /**
   * 准备态：模型下载/验校或引擎预热中。
   */
  object Preparing : RecognitionState("Preparing")

  /**
   * 就绪态：引擎完全初始化，可接收 start() 命令。
   */
  object ReadyToUse : RecognitionState("ReadyToUse")

  /**
   * 错误态：模型/引擎准备失败（可恢复）。
   */
  data class ModelPreparationFailed(
    val error: SpeechRecognitionEngineError,
  ) : RecognitionState("ModelPreparationFailed")

  /**
   * 监听态：已启动会话，正在捕获音频并识别。
   */
  object Listening : RecognitionState("Listening")

  /**
   * 完成态：识别流程已结束（正常或异常），等待 cleanup。
   */
  data class RecognitionFailed(
    val error: SpeechRecognitionEngineError,
  ) : RecognitionState("RecognitionFailed")

  /**
   * 释放态：已清理所有资源，客户端已调用 release()。
   */
  object Released : RecognitionState("Released")
}

/**
 * 状态机事件：驱动状态转移。
 */
internal sealed class RecognitionEvent {
  data object PrepareModel : RecognitionEvent()
  data object ModelPrepared : RecognitionEvent()
  data class ModelPreparationError(val error: SpeechRecognitionEngineError) : RecognitionEvent()
  data object StartListening : RecognitionEvent()
  data class ListeningError(val error: SpeechRecognitionEngineError) : RecognitionEvent()
  data object Cancel : RecognitionEvent()
  data object Completed : RecognitionEvent()
  data object Release : RecognitionEvent()
}

/**
 * 状态转移结果。
 */
internal sealed class TransitionResult {
  data object Success : TransitionResult()
  data class Ignored(val reason: String) : TransitionResult()
}