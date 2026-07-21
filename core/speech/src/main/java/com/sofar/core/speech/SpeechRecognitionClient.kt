package com.sofar.core.speech

import android.content.Context
import com.sofar.core.speech.internal.architecture.NoOpTelemetryHook
import com.sofar.core.speech.internal.architecture.RecognitionOrchestrator
import com.sofar.core.speech.internal.architecture.RecognitionUseCase
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.session.RecognitionSessionEvent
import com.sofar.core.speech.internal.session.RecognitionSessionOptions
import com.sofar.core.speech.internal.transcript.RecognitionTranscript
import com.sofar.core.speech.internal.transcript.TranscriptOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 语音识别客户端：公开 API 门面。
 *
 * **职责**：
 * - 提供简洁的 prepare/start/stop/cancel/release API
 * - 将用户调用委托给 RecognitionOrchestrator
 * - 转换 Flow<内部事件> 为 Flow<公开事件>
 *
 * **设计原则**：
 * - 无状态（状态全在 Orchestrator）
 * - 无工厂、无 flags、无历史包袱
 * - 每个方法对应一个 UseCase
 */
class SpeechRecognitionClient(
  context: Context,
  private val coroutineScope: CoroutineScope,
  request: SpeechRecognitionRequest = SpeechRecognitionRequest(),
  options: SpeechRecognitionOptions = SpeechRecognitionOptions(),
) {
  private val orchestrator = RecognitionOrchestrator(
    context = context.applicationContext,
    coroutineScope = coroutineScope,
    sessionOptions = RecognitionSessionOptions(
      restartDelayMillis = options.restartDelayMillis,
      finalizeDelayMillis = options.finalizeDelayMillis,
    ),
    transcriptOptions = TranscriptOptions(
      segmentSeparator = options.segmentSeparator,
      deduplicateFinalText = options.deduplicateFinalText,
    ),
    telemetry = NoOpTelemetryHook(),
  )

  private val config = SpeechRecognitionEngineConfig(
    engineType = request.engineType,
    languageCode = request.languageCode,
    sampleRate = request.sampleRate,
    preferOffline = request.preferOffline,
    enablePartialResult = request.enablePartialResult,
  )

  private val _events = MutableStateFlow<SpeechRecognitionEvent>(SpeechRecognitionEvent.Idle)
  val events: StateFlow<SpeechRecognitionEvent> = _events.asStateFlow()

  init {
    coroutineScope.launch {
      orchestrator.events.collect { event ->
        convertAndEmitEvent(event)
      }
    }
  }

  // 触发模型下载 + 引擎初始化（幂等，后台非阻塞）
  fun prepare() {
    orchestrator.execute(RecognitionUseCase.PrepareEngine(config))
  }

  // 启动识别：若未准备则自动 prepare，完成后开始录音
  fun start() {
    orchestrator.execute(RecognitionUseCase.StartRecognition(config))
  }

  // 优雅停止：等待当前识别完成
  fun stop() {
    orchestrator.execute(RecognitionUseCase.StopRecognition())
  }

  // 立即中断：丢弃当前识别结果
  fun cancel() {
    orchestrator.execute(RecognitionUseCase.CancelRecognition())
  }

  // 释放所有原生资源
  fun release() {
    orchestrator.execute(RecognitionUseCase.Release())
  }

  private fun convertAndEmitEvent(event: Any) {
    val publicEvent = mapPublicEvent(event) ?: return
    _events.tryEmit(publicEvent)
  }

  private fun mapPublicEvent(event: Any): SpeechRecognitionEvent? {
    return when (event) {
      is SpeechRecognitionEngineModelEvent.Checking -> SpeechRecognitionEvent.Checking
      is SpeechRecognitionEngineModelEvent.Downloading -> SpeechRecognitionEvent.Downloading(event.progress)
      is SpeechRecognitionEngineModelEvent.Unzipping -> SpeechRecognitionEvent.Unzipping
      is SpeechRecognitionEngineModelEvent.Ready -> SpeechRecognitionEvent.EngineReady
      is SpeechRecognitionEngineModelEvent.Error -> SpeechRecognitionEvent.Error(event.error.toPublicError())
      is RecognitionSessionEvent.Started -> SpeechRecognitionEvent.Started
      is RecognitionSessionEvent.TextChanged -> {
        SpeechRecognitionEvent.TranscriptChanged(event.transcript.toPublicTranscript())
      }

      is RecognitionSessionEvent.AudioLevelChanged -> SpeechRecognitionEvent.AudioLevelChanged(event.rmsDB)
      is RecognitionSessionEvent.Completed -> {
        SpeechRecognitionEvent.Completed(event.transcript.toPublicTranscript())
      }

      is RecognitionSessionEvent.Canceled -> SpeechRecognitionEvent.Canceled
      is RecognitionSessionEvent.Error -> SpeechRecognitionEvent.Error(event.error.toPublicError())
      else -> null
    }
  }

  private fun RecognitionTranscript.toPublicTranscript(): SpeechRecognitionTranscript {
    return SpeechRecognitionTranscript(
      text = text,
      finalSegments = finalSegments,
      partialText = partialText,
    )
  }

  private fun SpeechRecognitionEngineError.toPublicError(): SpeechRecognitionError {
    return SpeechRecognitionError(
      code = code,
      message = message,
      recoverable = recoverable,
      cause = cause,
    )
  }

  private companion object {
    private const val EVENT_BUFFER_CAPACITY = 64
  }
}

data class SpeechRecognitionRequest(
  val languageCode: String? = null,
  val preferOffline: Boolean = true,
  val engineType: SpeechEngineType = SpeechEngineType.SherpaOnnx,
  val sampleRate: Int = 16_000,
  val enablePartialResult: Boolean = true,
)

data class SpeechRecognitionOptions(
  val restartDelayMillis: Long = 180L,
  val finalizeDelayMillis: Long = 900L,
  val segmentSeparator: String = "  ·  ",
  val deduplicateFinalText: Boolean = false,
)

data class SpeechRecognitionTranscript(
  val text: String,
  val finalSegments: List<String>,
  val partialText: String,
)


sealed class SpeechRecognitionEvent {
  // 供统一交互使用的模型部署状态
  data object Idle : SpeechRecognitionEvent()                           // 初始态
  data object Checking : SpeechRecognitionEvent()                       // 正在检查模型是否存在
  data class Downloading(val progress: Int) : SpeechRecognitionEvent()  // 正在下载模型，带有 0-100 进度
  data object Unzipping : SpeechRecognitionEvent()                      // 下载完毕，正在解压 SenseVoice 资源
  data object EngineReady : SpeechRecognitionEvent()                    // 部署大功告成，三方离线引擎随时可用

  // 原有录音/识别状态
  data object Started : SpeechRecognitionEvent()
  data class TranscriptChanged(val transcript: SpeechRecognitionTranscript) :
    SpeechRecognitionEvent()

  data class AudioLevelChanged(val rmsDB: Float) : SpeechRecognitionEvent()
  data class Error(val error: SpeechRecognitionError) : SpeechRecognitionEvent()
  data class Completed(val transcript: SpeechRecognitionTranscript) : SpeechRecognitionEvent()
  data object Canceled : SpeechRecognitionEvent()
}

data class SpeechRecognitionError(
  val code: Int,
  val message: String,
  val recoverable: Boolean,
  val cause: Throwable? = null,
)
