package com.sofar.core.speech.internal.architecture

import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig

/**
 * 用例接口基类：应用层高层业务场景（只依赖 domain，不依赖具体实现）。
 *
 * 每个用例就是一个"动词短语"：
 * - PrepareRecognitionEngine：准备引擎（下载模型、初始化）
 * - StartRecognition：启动识别（开始捕音）
 * - StopRecognition：停止识别
 * - CancelRecognition：取消识别
 */
internal sealed class RecognitionUseCase {
  /**
   * 准备引擎用例：触发 prepare() 流程。
   *
   * 输出：Flow<SpeechRecognitionEngineModelEvent>
   *   - Checking
   *   - Downloading(progress: Int)
   *   - Unzipping
   *   - Ready
   *   - Error
   */
  data class PrepareEngine(val config: SpeechRecognitionEngineConfig = SpeechRecognitionEngineConfig()) : RecognitionUseCase()

  /**
   * 启动识别用例：触发 start() 流程。
   *
   * 输入：SpeechRecognitionEngineConfig（语言、采样率等）
   * 输出：Flow<SpeechRecognitionEngineEvent>
   *   - Ready（引擎已准备）
   *   - Started（音频流已启动）
   *   - Volume(rmsDB: Float)
   *   - Text(text: String, isFinal: Boolean)
   *   - End
   *   - Error
   *   - Completed
   */
  data class StartRecognition(val config: SpeechRecognitionEngineConfig) : RecognitionUseCase()

  /**
   * 停止识别用例：优雅停止（等待识别完成）。
   */
  class StopRecognition : RecognitionUseCase()

  /**
   * 取消识别用例：立即中断（丢弃当前会话）。
   */
  class CancelRecognition : RecognitionUseCase()

  /**
   * 释放资源用例：清理引擎、会话、音频源等。
   */
  class Release : RecognitionUseCase()
}