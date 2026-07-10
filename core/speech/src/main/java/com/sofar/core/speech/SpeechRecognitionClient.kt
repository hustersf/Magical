package com.sofar.core.speech

import android.content.Context
import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.engine.SpeechRecognitionEngineFactory
import com.sofar.core.speech.internal.policy.RecognitionSessionPolicyFactory
import com.sofar.core.speech.internal.session.RecognitionSessionEvent
import com.sofar.core.speech.internal.session.RecognitionSessionOptions
import com.sofar.core.speech.internal.session.SpeechRecognitionSession
import com.sofar.core.speech.internal.transcript.RecognitionTranscript
import com.sofar.core.speech.internal.transcript.RecognitionTranscriptAggregator
import com.sofar.core.speech.internal.transcript.TranscriptOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SpeechRecognitionClient(
  context: Context,
  private val coroutineScope: CoroutineScope,
  private val request: SpeechRecognitionRequest = SpeechRecognitionRequest(),
  private val options: SpeechRecognitionOptions = SpeechRecognitionOptions(),
) {
  private val appContext = context.applicationContext
  private val engineFactory = SpeechRecognitionEngineFactory(
    context = appContext,
    coroutineScope = coroutineScope,
  )

  private val _events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = EVENT_BUFFER_CAPACITY)
  val events: SharedFlow<SpeechRecognitionEvent> = _events.asSharedFlow()

  private var activeEngine: SpeechRecognitionEngine? = null
  private var recognitionSession: SpeechRecognitionSession? = null
  private var released: Boolean = false
  private var prepared: Boolean = false
  private var shouldStartAfterPrepared: Boolean = false

  val isRecording: Boolean
    get() = recognitionSession?.isRecording == true

  val isSessionActive: Boolean
    get() = recognitionSession?.isSessionActive == true

  fun prepare() {
    val engine = engineFactory.create(request.engineType)
    activeEngine = engine
    coroutineScope.launch {
      engine.prepare().collect { event ->
        when (event) {
          SpeechRecognitionEngineModelEvent.Checking -> {

          }

          is SpeechRecognitionEngineModelEvent.Downloading -> {

          }

          SpeechRecognitionEngineModelEvent.Unzipping -> {

          }

          SpeechRecognitionEngineModelEvent.Ready -> {
            prepared = true
            if (shouldStartAfterPrepared) {
              shouldStartAfterPrepared = false
              start()
            }
          }

          is SpeechRecognitionEngineModelEvent.Error -> {
            shouldStartAfterPrepared = false
          }
        }
      }
    }
  }

  fun start() {
    if (released) {
      Log.w(TAG, "start ignored: client already released")
      return
    }
    if (isSessionActive) {
      Log.d(TAG, "start ignored: session already active")
      return
    }
    if (!prepared) {
      Log.d(TAG, "start: client not ready, auto preparing...")
      shouldStartAfterPrepared = true
      prepare()
      return
    }
    val engine = activeEngine ?: engineFactory.create(request.engineType)
    Log.d(TAG, "start(request=$request)")
    recognitionSession = createSession(engine, request).also { it.start() }
  }

  fun stop() {
    Log.d(TAG, "stop")
    recognitionSession?.stop()
  }

  fun cancel() {
    Log.d(TAG, "cancel")
    recognitionSession?.cancel()
  }

  fun release() {
    Log.d(TAG, "release")
    val session = recognitionSession
    if (session != null) {
      session.release()
    } else {
      activeEngine?.release()
    }
    activeEngine = null
    recognitionSession = null
    released = true
  }

  private fun createSession(
    engine: SpeechRecognitionEngine,
    request: SpeechRecognitionRequest,
  ): SpeechRecognitionSession {
    return SpeechRecognitionSession(
      engine = engine,
      request = SpeechRecognitionEngineConfig(
        languageCode = request.languageCode,
        sampleRate = request.sampleRate,
        preferOffline = request.preferOffline,
        enablePartialResult = request.enablePartialResult && engine.capabilities.supportsPartialResult,
      ),
      policy = RecognitionSessionPolicyFactory.create(engine.capabilities),
      options = RecognitionSessionOptions(
        restartDelayMillis = options.restartDelayMillis,
        finalizeDelayMillis = options.finalizeDelayMillis,
      ),
      transcriptAggregator = RecognitionTranscriptAggregator(
        TranscriptOptions(
          segmentSeparator = options.segmentSeparator,
          deduplicateFinalText = options.deduplicateFinalText,
        ),
      ),
      coroutineScope = coroutineScope,
      eventSink = ::handleSessionEvent,
    )
  }

  private fun handleSessionEvent(event: RecognitionSessionEvent) {
    when (event) {
      RecognitionSessionEvent.Started -> {
        emitEvent(SpeechRecognitionEvent.Started)
      }

      is RecognitionSessionEvent.TextChanged -> {
        val publicTranscript = event.transcript.toPublicTranscript()
        emitEvent(SpeechRecognitionEvent.TranscriptChanged(publicTranscript))
      }

      is RecognitionSessionEvent.AudioLevelChanged -> {
        emitEvent(SpeechRecognitionEvent.AudioLevelChanged(event.rmsDB))
      }

      is RecognitionSessionEvent.Error -> {
        val publicError = SpeechRecognitionError(
          code = event.error.code,
          message = event.error.message,
          recoverable = event.error.recoverable,
          cause = event.error.cause,
        )
        emitEvent(SpeechRecognitionEvent.Error(publicError, event.hasRecognizedText))
      }

      is RecognitionSessionEvent.Completed -> {
        val publicTranscript = event.transcript.toPublicTranscript()
        activeEngine?.release()
        activeEngine = null
        recognitionSession = null
        emitEvent(SpeechRecognitionEvent.Completed(publicTranscript))
      }

      RecognitionSessionEvent.Canceled -> {
        activeEngine?.release()
        activeEngine = null
        recognitionSession = null
        emitEvent(SpeechRecognitionEvent.Canceled)
      }
    }
  }

  private fun emitEvent(event: SpeechRecognitionEvent) {
    _events.tryEmit(event)
  }

  private fun RecognitionTranscript.toPublicTranscript(): SpeechRecognitionTranscript {
    return SpeechRecognitionTranscript(
      text = text,
      finalSegments = finalSegments,
      partialText = partialText,
    )
  }

  private companion object {
    private const val TAG = "SpeechRecognitionClient"
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
) {
  val hasText: Boolean
    get() = text.isNotBlank()
}


sealed class SpeechRecognitionEvent {
  // 供统一交互使用的模型部署状态
  data object Checking : SpeechRecognitionEvent()                       // 正在检查模型是否存在
  data class Downloading(val progress: Int) : SpeechRecognitionEvent()  // 正在下载模型，带有 0-100 进度
  data object Unzipping : SpeechRecognitionEvent()                      // 下载完毕，正在解压 SenseVoice 资源
  data object EngineReady : SpeechRecognitionEvent()                    // 部署大功告成，三方离线引擎随时可用

  // 原有录音/识别状态
  data object Started : SpeechRecognitionEvent()
  data class TranscriptChanged(val transcript: SpeechRecognitionTranscript) : SpeechRecognitionEvent()
  data class AudioLevelChanged(val rmsDB: Float) : SpeechRecognitionEvent()
  data class Error(val error: SpeechRecognitionError, val hasRecognizedText: Boolean) : SpeechRecognitionEvent()
  data class Completed(val transcript: SpeechRecognitionTranscript) : SpeechRecognitionEvent()
  data object Canceled : SpeechRecognitionEvent()
}

data class SpeechRecognitionError(
  val code: Int,
  val message: String,
  val recoverable: Boolean,
  val cause: Throwable? = null,
)
