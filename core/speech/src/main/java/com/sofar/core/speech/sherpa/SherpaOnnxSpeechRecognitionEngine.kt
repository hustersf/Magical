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
import com.sofar.core.speech.internal.audio.AudioRecordAudioSource
import com.sofar.core.speech.internal.audio.AudioRecordConfig
import com.sofar.core.speech.internal.audio.AudioSource
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineSessionMode
import com.sofar.core.speech.internal.model.SpeechModel
import com.sofar.core.speech.internal.model.SpeechModelPathResolver
import com.sofar.core.speech.internal.model.SpeechModelValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

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

  /**
   * 线程模型：识别流程与资源缓存统一在单线程执行。
   * Threading model: recognition flow and resource cache are confined to one single thread.
   */

  override val capabilities: SpeechRecognitionEngineCapabilities =
    SpeechRecognitionEngineCapabilities(
      sessionMode = SpeechRecognitionEngineSessionMode.Continuous,
      supportsPartialResult = true,
      supportsVolume = true,
      supportsOffline = true,
      supportsLanguageSwitching = false,
    )

  // 引擎内部状态机专用执行线程。
  // Dedicated single thread for engine state and recognition flow.
  private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  private val engineScope = CoroutineScope(singleThreadDispatcher + SupervisorJob())

  // 会话状态仅在引擎单线程内读写，避免跨线程共享可变状态。
  // Session states are confined to the engine single thread.
  private var currentAudioSource: AudioSource? = null
  private var canceled: Boolean = false

  // 采样率维度的资源缓存（仅单线程访问）。
  // Sample-rate keyed resource cache (single-thread only).
  private val preparedResourcesBySampleRate: MutableMap<Int, EngineResources> = linkedMapOf()

  // 上一次 start 流程的 Job，用于串行切换会话。
  // Previous start job used to serialize session handover.
  private var previousStartJob: Job? = null

  // 预热模型与基础资源。
  // Prepares model assets and runtime resources.
  override fun prepare(): Flow<SpeechRecognitionEngineModelEvent> = flow {
    canceled = false

    val validation = modelValidator.validate(model)
    if (!validation.isValid) {
      emit(
        SpeechRecognitionEngineModelEvent.Error(
          error(
            ERROR_INVALID_MODEL,
            validation.message.orEmpty()
          )
        )
      )
      return@flow
    }

    if (canceled) {
      return@flow
    }

    val preparation = prepareResources(sampleRate = DEFAULT_SAMPLE_RATE)

    if (canceled) {
      return@flow
    }

    when (preparation) {
      is ResourcePreparationResult.Success -> emit(SpeechRecognitionEngineModelEvent.Ready)
      is ResourcePreparationResult.Failure -> emit(
        SpeechRecognitionEngineModelEvent.Error(
          preparation.error
        )
      )
    }
  }.flowOn(singleThreadDispatcher)

  // 启动连续识别会话。
  // Starts a continuous recognition session.
  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> =
    callbackFlow {
      // 串行等待旧会话完全结束，避免会话切换窗口内的并发冲突。
      // Waits previous session cleanup before starting a new one.
      val jobToAwait = previousStartJob
      previousStartJob = currentCoroutineContext()[Job]
      jobToAwait?.join()

      val validation = modelValidator.validate(model)
      if (!validation.isValid) {
        trySend(
          SpeechRecognitionEngineEvent.Error(
            error(
              ERROR_INVALID_MODEL,
              validation.message.orEmpty()
            )
          )
        )
        close()
        return@callbackFlow
      }

      canceled = false

      if (currentAudioSource != null) {
        trySend(
          SpeechRecognitionEngineEvent.Error(
            error(ERROR_BUSY, "Recognition already running", recoverable = false),
          ),
        )
        close()
        return@callbackFlow
      }

      try {
        val audioSource = audioSourceFactory(config)
        currentAudioSource = audioSource

        val preparedResources = prepareResources(sampleRate = config.sampleRate)
        val resources = when (preparedResources) {
          is ResourcePreparationResult.Success -> preparedResources.resources
          is ResourcePreparationResult.Failure -> {
            trySend(SpeechRecognitionEngineEvent.Error(preparedResources.error))
            close()
            return@callbackFlow
          }
        }

        if (canceled) {
          close()
          return@callbackFlow
        }

        val recognizer = resources.recognizer
        val vad = resources.vad
        vad.reset()
        vad.clear()

        if (canceled) {
          close()
          return@callbackFlow
        }

        trySend(SpeechRecognitionEngineEvent.Ready)

        val activeSpeechBuffer = FloatSampleBuffer()
        var lastPartialText = ""
        var lastPartialDecodeMillis = 0L
        var startedSent = false

        runCatching {
          audioSource.start().collect { frame ->
            if (canceled) {
              return@collect
            }
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
                val partialText =
                  decodeSamples(recognizer, frame.sampleRate, activeSpeechBuffer.toFloatArray())
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
            trySend(
              SpeechRecognitionEngineEvent.Error(
                error(
                  ERROR_RUNTIME,
                  "sherpa-onnx recognition failed",
                  throwable
                )
              )
            )
          }
        }

        trySend(SpeechRecognitionEngineEvent.End)
        close()
      } catch (cancellation: CancellationException) {
        Log.w(TAG, "start canceled", cancellation)
        throw cancellation
      } catch (throwable: Throwable) {
        Log.w(TAG, "start failed", throwable)
        if (!canceled) {
          trySend(
            SpeechRecognitionEngineEvent.Error(
              error(
                ERROR_RUNTIME,
                "sherpa-onnx start failed",
                throwable
              )
            )
          )
        }
        close()
      } finally {
        releaseCurrentAudioSource()
      }
    }.flowOn(singleThreadDispatcher)

  // 请求会话收尾，不清空缓存资源。
  // Requests graceful session stop without clearing prepared resources.
  override fun stop() {
    submitControlCommand {
      currentAudioSource?.stop()
    }
  }

  // 立即中断当前会话，保留引擎缓存。
  // Cancels current session immediately while preserving prepared cache.
  override fun cancel() {
    submitControlCommand {
      canceled = true
      currentAudioSource?.stop()
    }
  }

  // 在引擎线程异步释放当前会话与缓存资源。
  // Releases session and cached resources asynchronously on engine thread.
  override fun release() {
    submitControlCommand {
      canceled = true
      releaseCurrent()
    }
  }

  private fun submitControlCommand(block: () -> Unit) {
    engineScope.launch {
      block()
    }
  }

  // 清理会话与缓存。
  // Clears active session and cached resources.
  private fun releaseCurrent() {
    releaseCurrentAudioSource()
    releasePreparedResources()
  }

  // 释放当前音频源。
  // Releases active audio source.
  private fun releaseCurrentAudioSource() {
    currentAudioSource?.release()
    currentAudioSource = null
  }

  // 释放并清空采样率缓存。
  // Releases and clears sample-rate cache.
  private fun releasePreparedResources() {
    val resources = preparedResourcesBySampleRate.values.toList()
    preparedResourcesBySampleRate.clear()
    resources.forEach(::releaseResources)
  }

  // 获取或构建指定采样率资源，缓存大小受 LRU 上限控制。
  // Gets or creates resources for sample rate with bounded LRU cache.
  private fun prepareResources(sampleRate: Int): ResourcePreparationResult {
    val cached = preparedResourcesBySampleRate[sampleRate]
    if (cached != null) return ResourcePreparationResult.Success(cached)

    val modelDir = modelPathResolver.resolveModelDir(model)
    val recognizer = runCatching {
      createOfflineRecognizer(model.sherpaConfig, sampleRate, modelDir)
    }.getOrElse { throwable ->
      return ResourcePreparationResult.Failure(
        error(ERROR_CREATE_RECOGNIZER, "Create sherpa-onnx recognizer failed", throwable),
      )
    }

    val vad = runCatching {
      createVad(sampleRate, modelDir)
    }.getOrElse { throwable ->
      recognizer.release()
      return ResourcePreparationResult.Failure(
        error(ERROR_CREATE_VAD, "Create sherpa-onnx VAD failed", throwable),
      )
    }

    val created = EngineResources(recognizer = recognizer, vad = vad)
    var evicted: EngineResources? = null

    val cachedOrCreated = preparedResourcesBySampleRate[sampleRate] ?: created.also {
      if (preparedResourcesBySampleRate.size >= MAX_PREPARED_RESOURCE_CACHE_SIZE) {
        val eldestKey = preparedResourcesBySampleRate.keys.firstOrNull()
        if (eldestKey != null) {
          evicted = preparedResourcesBySampleRate.remove(eldestKey)
        }
      }
      preparedResourcesBySampleRate[sampleRate] = it
    }

    evicted?.let(::releaseResources)
    return ResourcePreparationResult.Success(cachedOrCreated)
  }

  // 释放识别器与 VAD 资源。
  // Releases recognizer and VAD resources.
  private fun releaseResources(resources: EngineResources) {
    resources.recognizer.release()
    resources.vad.release()
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
    return SpeechRecognitionEngineError(
      code = code,
      message = message,
      cause = cause,
      recoverable = recoverable
    )
  }

  private companion object {
    private const val TAG = "SherpaOnnxSpeechRecognitionEngine"
    private const val DEFAULT_SAMPLE_RATE = 16_000
    private const val ERROR_INVALID_MODEL = 10_002
    private const val ERROR_CREATE_RECOGNIZER = 10_003
    private const val ERROR_CREATE_VAD = 10_004
    private const val ERROR_RUNTIME = 10_005
    private const val ERROR_BUSY = 10_006
    private const val PARTIAL_DECODE_INTERVAL_MILLIS = 320L
    private const val MAX_PREPARED_RESOURCE_CACHE_SIZE = 2
  }
}

private data class EngineResources(
  val recognizer: OfflineRecognizer,
  val vad: Vad,
)

private sealed class ResourcePreparationResult {
  data class Success(val resources: EngineResources) : ResourcePreparationResult()
  data class Failure(val error: SpeechRecognitionEngineError) : ResourcePreparationResult()
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