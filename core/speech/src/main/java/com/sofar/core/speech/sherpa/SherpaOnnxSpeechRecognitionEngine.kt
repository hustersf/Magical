package com.sofar.core.speech.sherpa

import android.content.Context
import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEvent
import com.sofar.core.speech.internal.audio.AudioRecordAudioSource
import com.sofar.core.speech.internal.audio.AudioRecordConfig
import com.sofar.core.speech.internal.audio.AudioSource
import com.sofar.core.speech.internal.model.SpeechModelManager
import com.sofar.core.speech.internal.model.SpeechModelSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal class SherpaOnnxSpeechRecognitionEngine(
  context: Context,
  private val modelManager: SpeechModelManager,
  private val runtimeConfig: SherpaOnnxConfig = SherpaOnnxConfig(),
  private val recognitionFactory: SherpaOnnxRecognitionFactory = ReflectiveSherpaOnnxRecognitionFactory(context.assets),
  private val audioSourceFactory: (SpeechRecognitionConfig) -> AudioSource = { config ->
    AudioRecordAudioSource(
      context = context.applicationContext,
      config = AudioRecordConfig(sampleRate = config.sampleRate),
    )
  },
) : SpeechRecognitionEngine {

  private var currentAudioSource: AudioSource? = null
  private var currentRecognition: SherpaOnnxRecognition? = null
  @Volatile private var canceled: Boolean = false

  override fun start(config: SpeechRecognitionConfig): Flow<SpeechRecognitionEvent> = callbackFlow {
    val modelId = config.modelId
    if (modelId.isNullOrBlank()) {
      trySend(SpeechRecognitionEvent.Error(error(ERROR_NO_MODEL, "modelId is required for sherpa-onnx")))
      close()
      return@callbackFlow
    }

    val model = runCatching { modelManager.require(modelId) }.getOrElse { throwable ->
      trySend(SpeechRecognitionEvent.Error(error(ERROR_NO_MODEL, throwable.message.orEmpty(), throwable)))
      close()
      return@callbackFlow
    }

    val validation = modelManager.validate(model)
    if (!validation.isValid) {
      trySend(SpeechRecognitionEvent.Error(error(ERROR_INVALID_MODEL, validation.message.orEmpty())))
      close()
      return@callbackFlow
    }

    canceled = false
    val audioSource = audioSourceFactory(config)
    val recognition = runCatching {
      recognitionFactory.create(
        modelConfig = model.sherpaConfig,
        runtimeConfig = runtimeConfig.copy(sampleRate = config.sampleRate),
        modelDir = modelManager.resolveModelDir(model),
        useAssetManager = model.source is SpeechModelSource.Assets,
      )
    }.getOrElse { throwable ->
      trySend(SpeechRecognitionEvent.Error(error(ERROR_CREATE_RECOGNIZER, "Create sherpa-onnx recognizer failed", throwable)))
      close()
      return@callbackFlow
    }

    currentAudioSource = audioSource
    currentRecognition = recognition
    trySend(SpeechRecognitionEvent.Ready)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    var lastPartialText = ""
    scope.launch {
      runCatching {
        audioSource.start().collect { frame ->
          trySend(SpeechRecognitionEvent.Volume(frame.rmsDb))
          recognition.acceptWaveform(frame.sampleRate, frame.samples)
          recognition.decode()
          val text = recognition.getText().trim()
          if (config.enablePartialResult && text.isNotEmpty() && text != lastPartialText) {
            lastPartialText = text
            trySend(SpeechRecognitionEvent.Text(text, isFinal = false))
          }
          if (recognition.isEndpoint()) {
            if (text.isNotEmpty()) {
              trySend(SpeechRecognitionEvent.Text(text, isFinal = true))
            }
            recognition.reset()
            lastPartialText = ""
          }
        }
      }.onFailure { throwable ->
        if (!canceled) {
          Log.w(TAG, "sherpa-onnx recognition failed", throwable)
          trySend(SpeechRecognitionEvent.Error(error(ERROR_RUNTIME, "sherpa-onnx recognition failed", throwable)))
        }
      }
      trySend(SpeechRecognitionEvent.End)
      close()
    }

    awaitClose {
      scope.cancel()
      releaseCurrent()
    }
  }

  override fun stop() {
    currentAudioSource?.stop()
  }

  override fun cancel() {
    canceled = true
    currentAudioSource?.stop()
  }

  override fun release() {
    releaseCurrent()
  }

  private fun releaseCurrent() {
    currentAudioSource?.release()
    currentAudioSource = null
    currentRecognition?.release()
    currentRecognition = null
  }

  private fun error(
    code: Int,
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
  ): SpeechRecognitionError {
    return SpeechRecognitionError(code = code, message = message, cause = cause, recoverable = recoverable)
  }

  private companion object {
    private const val TAG = "SherpaOnnxSpeechRecognitionEngine"
    private const val ERROR_NO_MODEL = 10_001
    private const val ERROR_INVALID_MODEL = 10_002
    private const val ERROR_CREATE_RECOGNIZER = 10_003
    private const val ERROR_RUNTIME = 10_004
  }
}