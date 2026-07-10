package com.sofar.core.speech.sherpa

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineParaformerModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.OfflineZipformerCtcModelConfig
import com.k2fsa.sherpa.onnx.SherpaNativeLoader
import com.k2fsa.sherpa.onnx.SileroVadModelConfig
import com.k2fsa.sherpa.onnx.TenVadModelConfig
import com.k2fsa.sherpa.onnx.Vad
import com.k2fsa.sherpa.onnx.VadModelConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineSessionMode
import com.sofar.core.speech.internal.audio.AudioRecordAudioSource
import com.sofar.core.speech.internal.audio.AudioRecordConfig
import com.sofar.core.speech.internal.audio.AudioSource
import com.sofar.core.speech.internal.model.SpeechModel
import com.sofar.core.speech.internal.model.SpeechModelPathResolver
import com.sofar.core.speech.internal.model.SpeechModelValidator
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
  private val model: SpeechModel,
  private val modelPathResolver: SpeechModelPathResolver = SpeechModelPathResolver(),
  private val modelValidator: SpeechModelValidator = SpeechModelValidator(modelPathResolver),
  private val runtimeConfig: SherpaOnnxConfig = SherpaOnnxConfig(),
  private val audioSourceFactory: (SpeechRecognitionEngineConfig) -> AudioSource = { config ->
    AudioRecordAudioSource(
      context = context.applicationContext,
      config = AudioRecordConfig(sampleRate = config.sampleRate),
    )
  },
) : SpeechRecognitionEngine {

  override val capabilities: SpeechRecognitionEngineCapabilities = SpeechRecognitionEngineCapabilities(
    sessionMode = SpeechRecognitionEngineSessionMode.Continuous,
    supportsPartialResult = true,
    supportsVolume = true,
    supportsOffline = true,
    supportsLanguageSwitching = false,
  )

  private var currentAudioSource: AudioSource? = null
  private var currentRecognizer: OfflineRecognizer? = null
  private var currentVad: Vad? = null
  @Volatile private var canceled: Boolean = false

  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> = callbackFlow {
    val validation = modelValidator.validate(model)
    if (!validation.isValid) {
      trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_INVALID_MODEL, validation.message.orEmpty())))
      close()
      return@callbackFlow
    }

    canceled = false
    val audioSource = audioSourceFactory(config)
    val modelDir = modelPathResolver.resolveModelDir(model)
    val recognizer = runCatching {
      createOfflineRecognizer(model.sherpaConfig, config.sampleRate, modelDir)
    }.getOrElse { throwable ->
      trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_CREATE_RECOGNIZER, "Create sherpa-onnx recognizer failed", throwable)))
      close()
      return@callbackFlow
    }

    val vad = runCatching {
      createVad(config.sampleRate, modelDir)
    }.getOrElse { throwable ->
      recognizer.release()
      trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_CREATE_VAD, "Create sherpa-onnx VAD failed", throwable)))
      close()
      return@callbackFlow
    }

    currentAudioSource = audioSource
    currentRecognizer = recognizer
    currentVad = vad
    trySend(SpeechRecognitionEngineEvent.Ready)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val activeSpeechBuffer = FloatSampleBuffer()
    var lastPartialText = ""
    var lastPartialDecodeMillis = 0L
    var startedSent = false
    scope.launch {
      runCatching {
        audioSource.start().collect { frame ->
          if (!startedSent) {
            startedSent = true
            trySend(SpeechRecognitionEngineEvent.Started)
          }
          trySend(SpeechRecognitionEngineEvent.Volume(frame.rmsDb))
          vad.acceptWaveform(frame.samples)

          if (vad.isSpeechDetected()) {
            activeSpeechBuffer.append(frame.samples)
          }

          if (config.enablePartialResult && activeSpeechBuffer.isNotEmpty()) {
            val shouldDecodePartial =
              frame.timestampMillis - lastPartialDecodeMillis >= PARTIAL_DECODE_INTERVAL_MILLIS
            if (shouldDecodePartial) {
              val partialText = decodeSamples(recognizer, frame.sampleRate, activeSpeechBuffer.toFloatArray())
              if (partialText.isNotEmpty() && partialText != lastPartialText) {
                lastPartialText = partialText
                trySend(SpeechRecognitionEngineEvent.Text(partialText, isFinal = false))
              }
              lastPartialDecodeMillis = frame.timestampMillis
            }
          }

          drainVadSegments(vad) { segmentSamples ->
            val finalText = decodeSamples(recognizer, frame.sampleRate, segmentSamples)
            if (finalText.isNotEmpty()) {
              trySend(SpeechRecognitionEngineEvent.Text(finalText, isFinal = true))
            }
            activeSpeechBuffer.clear()
            lastPartialText = ""
          }
        }

        vad.flush()
        drainVadSegments(vad) { segmentSamples ->
          val finalText = decodeSamples(recognizer, config.sampleRate, segmentSamples)
          if (finalText.isNotEmpty()) {
            trySend(SpeechRecognitionEngineEvent.Text(finalText, isFinal = true))
          }
        }
      }.onFailure { throwable ->
        if (!canceled) {
          Log.w(TAG, "sherpa-onnx recognition failed", throwable)
          trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_RUNTIME, "sherpa-onnx recognition failed", throwable)))
        }
      }
      trySend(SpeechRecognitionEngineEvent.End)
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
    currentRecognizer?.release()
    currentRecognizer = null
    currentVad?.release()
    currentVad = null
  }

  private fun createOfflineRecognizer(
    modelConfig: SherpaOnnxModelConfig,
    sampleRate: Int,
    modelDir: String,
  ): OfflineRecognizer {
    SherpaNativeLoader.ensureLoaded()
    val offlineModelConfig = OfflineModelConfig().apply {
      tokens = resolvePath(modelDir, modelConfig.tokens)
      numThreads = runtimeConfig.numThreads
      provider = runtimeConfig.provider
      debug = runtimeConfig.debug
      modelType = modelConfig.modelType.toOfflineModelType()
      when (modelConfig.modelType) {
        SherpaOnnxModelType.SenseVoice -> {
          senseVoice = OfflineSenseVoiceModelConfig(
            model = resolvePath(modelDir, modelConfig.model.orEmpty()),
          )
        }

        SherpaOnnxModelType.Transducer -> {
          transducer = OfflineTransducerModelConfig(
            encoder = resolvePath(modelDir, modelConfig.encoder.orEmpty()),
            decoder = resolvePath(modelDir, modelConfig.decoder.orEmpty()),
            joiner = resolvePath(modelDir, modelConfig.joiner.orEmpty()),
          )
        }

        SherpaOnnxModelType.Paraformer -> {
          paraformer = OfflineParaformerModelConfig(
            model = resolvePath(modelDir, modelConfig.model.orEmpty()),
          )
        }

        SherpaOnnxModelType.Zipformer2Ctc,
        SherpaOnnxModelType.Ctc -> {
          zipformerCtc = OfflineZipformerCtcModelConfig(
            model = resolvePath(modelDir, modelConfig.model.orEmpty()),
          )
        }

        SherpaOnnxModelType.Whisper -> {
          whisper = OfflineWhisperModelConfig(
            encoder = resolvePath(modelDir, modelConfig.encoder.orEmpty()),
            decoder = resolvePath(modelDir, modelConfig.decoder.orEmpty()),
            language = "",
          )
        }
      }
    }

    val recognizerConfig = OfflineRecognizerConfig(
      featConfig = FeatureConfig(sampleRate = sampleRate, featureDim = runtimeConfig.featureDim),
      modelConfig = offlineModelConfig,
      decodingMethod = runtimeConfig.decodingMethod,
    )
    return OfflineRecognizer(recognizerConfig)
  }

  private fun createVad(
    sampleRate: Int,
    modelDir: String,
  ): Vad {
    SherpaNativeLoader.ensureLoaded()
    val vadPath = resolvePath(modelDir, runtimeConfig.vadModelPath)
    return Vad(
      VadModelConfig(
        sampleRate = sampleRate,
        numThreads = runtimeConfig.numThreads,
        provider = runtimeConfig.provider,
        debug = runtimeConfig.debug,
        sileroVadModelConfig = SileroVadModelConfig(
          model = vadPath,
          threshold = runtimeConfig.vadThreshold,
          minSilenceDuration = runtimeConfig.vadMinSilenceDuration,
          minSpeechDuration = runtimeConfig.vadMinSpeechDuration,
          maxSpeechDuration = runtimeConfig.vadMaxSpeechDuration,
        ),
        tenVadModelConfig = TenVadModelConfig(
          model = "",
        ),
      ),
    )
  }

  private fun decodeSamples(
    recognizer: OfflineRecognizer,
    sampleRate: Int,
    samples: FloatArray,
  ): String {
    if (samples.isEmpty()) return ""
    val stream = recognizer.createStream()
    return try {
      stream.acceptWaveform(samples = samples, sampleRate = sampleRate)
      recognizer.decode(stream)
      recognizer.getResult(stream).text.trim()
    } finally {
      stream.release()
    }
  }

  private inline fun drainVadSegments(vad: Vad, consume: (FloatArray) -> Unit) {
    while (!vad.empty()) {
      val segment = vad.front()
      vad.pop()
      if (segment.samples.isNotEmpty()) {
        consume(segment.samples)
      }
    }
  }

  private fun resolvePath(modelDir: String, relativePath: String): String {
    if (relativePath.isBlank()) return relativePath
    val normalizedModelDir = modelDir.trimEnd('/')
    val normalizedRelative = relativePath.trimStart('/')
    return java.io.File(normalizedModelDir, normalizedRelative).absolutePath
  }

  private fun SherpaOnnxModelType.toOfflineModelType(): String {
    return when (this) {
      SherpaOnnxModelType.SenseVoice -> ""
      SherpaOnnxModelType.Transducer -> "transducer"
      SherpaOnnxModelType.Paraformer -> "paraformer"
      SherpaOnnxModelType.Zipformer2Ctc -> "zipformer2_ctc"
      SherpaOnnxModelType.Ctc -> "ctc"
      SherpaOnnxModelType.Whisper -> "whisper"
    }
  }

  private fun error(
    code: Int,
    message: String,
    cause: Throwable? = null,
    recoverable: Boolean = false,
  ): SpeechRecognitionEngineError {
    return SpeechRecognitionEngineError(code = code, message = message, cause = cause, recoverable = recoverable)
  }

  private companion object {
    private const val TAG = "SherpaOnnxSpeechRecognitionEngine"
    private const val ERROR_INVALID_MODEL = 10_002
    private const val ERROR_CREATE_RECOGNIZER = 10_003
    private const val ERROR_CREATE_VAD = 10_004
    private const val ERROR_RUNTIME = 10_005
    private const val PARTIAL_DECODE_INTERVAL_MILLIS = 320L
  }
}

private class FloatSampleBuffer(
  initialCapacity: Int = 16_000,
) {
  private var data: FloatArray = FloatArray(initialCapacity.coerceAtLeast(1))
  private var size: Int = 0

  fun append(values: FloatArray) {
    if (values.isEmpty()) return
    ensureCapacity(size + values.size)
    values.copyInto(data, destinationOffset = size)
    size += values.size
  }

  fun clear() {
    size = 0
  }

  fun isNotEmpty(): Boolean = size > 0

  fun toFloatArray(): FloatArray = data.copyOf(size)

  private fun ensureCapacity(required: Int) {
    if (required <= data.size) return
    var newCapacity = data.size
    while (newCapacity < required) {
      newCapacity = (newCapacity * 2).coerceAtLeast(required)
    }
    data = data.copyOf(newCapacity)
  }
}