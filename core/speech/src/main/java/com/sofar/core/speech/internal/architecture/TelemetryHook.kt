package com.sofar.core.speech.internal.architecture

import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError

/**
 * 观测接口：允许业务针对识别流程注入自定义行为（日志、指标、重试、恢复等）。
 *
 * 设计原则（观测器模式）：
 * - 非侵入：引擎层无需感知
 * - 可组合：支持多个 listener 并行
 * - 异步友好：所有方法非 suspend，不阻塞主流程
 */
internal interface TelemetryHook {
  /**
   * 状态转移钩子。
   */
  fun onStateTransition(fromState: RecognitionState, toState: RecognitionState) {}

  /**
   * 准备开始。
   */
  fun onPrepareStart() {}

  /**
   * 准备进度（如模型下载）。
   */
  fun onPrepareProgress(stage: String, progress: Int) {}

  /**
   * 准备完成。
   */
  fun onPrepareComplete() {}

  /**
   * 准备失败。
   */
  fun onPrepareFailed(error: SpeechRecognitionEngineError, recoverable: Boolean) {}

  /**
   * 识别启动。
   */
  fun onRecognitionStart() {}

  /**
   * 识别中间结果（部分文本）。
   */
  fun onRecognitionPartial(text: String) {}

  /**
   * 识别最终结果。
   */
  fun onRecognitionFinal(text: String) {}

  /**
   * 识别失败。
   */
  fun onRecognitionFailed(error: SpeechRecognitionEngineError) {}

  /**
   * 音量变化（可用于 UI 波形绘制）。
   */
  fun onAudioVolumeChanged(rmsDB: Float) {}

  /**
   * 引擎降级到 fallback（例如：Sherpa 失败，切到 Android）。
   */
  fun onEngineFallback(fromEngine: String, toEngine: String, reason: String) {}

  /**
   * 资源释放。
   */
  fun onResourceReleased() {}
}

/**
 * 无操作的空实现（装饰者模式中的 Null Object）。
 */
internal class NoOpTelemetryHook : TelemetryHook