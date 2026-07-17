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
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

  override val capabilities: SpeechRecognitionEngineCapabilities = SpeechRecognitionEngineCapabilities(
    sessionMode = SpeechRecognitionEngineSessionMode.Continuous,
    supportsPartialResult = true,
    supportsVolume = true,
    supportsOffline = true,
    supportsLanguageSwitching = false,
  )

  // 单线程执行所有操作，消除 start() 内部的竞态条件。
  private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

  // stop()/cancel()/release() 从外部线程调用，需要 @Volatile 保证跨线程可见性。
  // 单线程保证 start() 内部的写入顺序，@Volatile 保证外部线程读取时看到最新值。
  @Volatile private var currentAudioSource: AudioSource? = null
  @Volatile private var canceled: Boolean = false
  // LRU 缓存：限制原生资源数量，避免多采样率场景无限增长。
  // preparedResourcesBySampleRate 由单线程写入、由 release()（外部线程）清空，需要锁保护。
  private val preparedResourcesBySampleRate: MutableMap<Int, EngineResources> = linkedMapOf()
  private val resourcesLock = Any()
  // 上一次 start() 的 producer Job，仅在单线程上访问，无需 @Volatile。
  // 新 start() 通过 join() 等待上一次清理完成，避免 stop→start 时序窗口导致 ERROR_BUSY。
  private var previousStartJob: Job? = null

  // 验证模型 → 创建识别器和 VAD（单线程）
  override fun prepare(): Flow<SpeechRecognitionEngineModelEvent> = flow {
    // 每次 prepare 视为新一轮请求，先清掉历史 cancel 标记。
    canceled = false

    val validation = modelValidator.validate(model)
    if (!validation.isValid) {
      emit(SpeechRecognitionEngineModelEvent.Error(error(ERROR_INVALID_MODEL, validation.message.orEmpty())))
      return@flow
    }

    // Preparing 阶段检查 canceled 标志。多次 cancel 时，prepare() 应尽早返回，避免状态污染。
    if (canceled) {
      return@flow
    }

    val preparation = prepareResources(sampleRate = DEFAULT_SAMPLE_RATE)

    // prepare() 完成后再次检查 canceled，如果被取消则不发出 Ready，让 Orchestrator 处理取消。
    if (canceled) {
      return@flow
    }

    when (preparation) {
      is ResourcePreparationResult.Success -> emit(SpeechRecognitionEngineModelEvent.Ready)
      is ResourcePreparationResult.Failure -> emit(SpeechRecognitionEngineModelEvent.Error(preparation.error))
    }
  }.flowOn(singleThreadDispatcher)

  // 启动连续识别：
  // 1. join() 等待上次 start 清理完成 → 2. AudioSource → 3. Vad+识别循环 → 4. 部分/最终文本及音量
  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> = callbackFlow {
    // 等待上一次 start() 的 producer Job 完全结束（包括 finally 清理）。
    //
    // 问题根因：Dispatchers.Main.immediate 使新 start() 立即执行，
    // 新 producer 提交到 singleThreadDispatcher 时，旧 producer 的取消信号尚未到达
    // （cancel 传播跨越 Main→flowOn 是异步的），导致队列顺序变成：
    //   [Task2: 新 producer] → [Task1: 旧 cancel]
    // Task2 先执行，看到 currentAudioSource != null → ERROR_BUSY。
    //
    // 修复：新 producer 通过 join() 等待旧 Job 结束。join() 是挂起函数，
    // 在单线程上 join() 会让出线程，让旧 producer 的 cleanup 先执行，
    // finally { releaseCurrentAudioSource() } 完成后 join() 才返回。
    val jobToAwait = previousStartJob
    previousStartJob = currentCoroutineContext()[Job]
    jobToAwait?.join()

    val validation = modelValidator.validate(model)
    if (!validation.isValid) {
      trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_INVALID_MODEL, validation.message.orEmpty())))
      close()
      return@callbackFlow
    }

    // 新一轮 start，重置取消标志。
    canceled = false

    if (currentAudioSource != null) {
      // double-check：join() 之后仍然非 null，说明有真实的并发 start()，拒绝。
      trySend(
        SpeechRecognitionEngineEvent.Error(
          error(ERROR_BUSY, "Recognition already running", recoverable = false),
        ),
      )
      close()
      return@callbackFlow
    }

    try {
      // 创建 AudioSource，赋值，使 cancel() 能立即生效
      val audioSource = audioSourceFactory(config)
      currentAudioSource = audioSource

      // 准备资源（识别器和 VAD）
      val preparedResources = prepareResources(sampleRate = config.sampleRate)
      val resources = when (preparedResources) {
        is ResourcePreparationResult.Success -> preparedResources.resources
        is ResourcePreparationResult.Failure -> {
          trySend(SpeechRecognitionEngineEvent.Error(preparedResources.error))
          releaseCurrentAudioSource()
          close()
          return@callbackFlow
        }
      }

      if (canceled) {
        releaseCurrentAudioSource()
        close()
        return@callbackFlow
      }

      val recognizer = resources.recognizer
      val vad = resources.vad
      vad.reset()
      vad.clear()

      if (canceled) {
        releaseCurrentAudioSource()
        close()
        return@callbackFlow
      }

      trySend(SpeechRecognitionEngineEvent.Ready)

      // 在单线程上下文中直接执行识别循环
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
    } catch (cancellation: CancellationException) {
      Log.w(TAG, "start canceled", cancellation)
      releaseCurrentAudioSource()
      throw cancellation
    } catch (throwable: Throwable) {
      Log.w(TAG, "start failed", throwable)
      releaseCurrentAudioSource()
      if (!canceled) {
        trySend(SpeechRecognitionEngineEvent.Error(error(ERROR_RUNTIME, "sherpa-onnx start failed", throwable)))
      }
      close()
    } finally {
      releaseCurrentAudioSource()
    }
  }.flowOn(singleThreadDispatcher)

  // 优雅停止：仅停止 AudioSource，不标记 canceled，保留尾帧 flush 机会。
  override fun stop() {
    currentAudioSource?.stop()
  }

  // 立即中断：设置 canceled 标志 + 停止音频采集。
  // 注意：Cancel 仅释放 Session，Engine 及其已准备资源需要保留，不能清缓存。
  override fun cancel() {
    canceled = true
    currentAudioSource?.stop()
  }

  // 释放所有资源：清空音频源和缓存资源（LRU）
  override fun release() {
    canceled = true
    releaseCurrent()
  }

  // 清理当前音频源和所有预准备资源
  private fun releaseCurrent() {
    releaseCurrentAudioSource()
    releasePreparedResources()
  }

  // 释放当前音频源引用
  private fun releaseCurrentAudioSource() {
    currentAudioSource?.release()
    currentAudioSource = null
  }

  // 清空所有采样率的 LRU 缓存（识别器+VAD）
  private fun releasePreparedResources() {
    val resources = synchronized(resourcesLock) {
      preparedResourcesBySampleRate.values.toList().also { preparedResourcesBySampleRate.clear() }
    }
    resources.forEach(::releaseResources)
  }

  // 准备采样率对应的识别器+VAD（支持多采样率，LRU 缓存大小≤2，超出自动清除最旧）
  private fun prepareResources(sampleRate: Int): ResourcePreparationResult {
    // 在单线程中直接访问缓存，无需同步
    val cached = synchronized(resourcesLock) { preparedResourcesBySampleRate[sampleRate] }
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

    synchronized(resourcesLock) {
      preparedResourcesBySampleRate[sampleRate] ?: created.also {
        if (preparedResourcesBySampleRate.size >= MAX_PREPARED_RESOURCE_CACHE_SIZE) {
          val eldestKey = preparedResourcesBySampleRate.keys.firstOrNull()
          if (eldestKey != null) {
            evicted = preparedResourcesBySampleRate.remove(eldestKey)
          }
        }
        preparedResourcesBySampleRate[sampleRate] = it
      }
    }

    evicted?.let(::releaseResources)
    return ResourcePreparationResult.Success(
      synchronized(resourcesLock) { preparedResourcesBySampleRate[sampleRate] } ?: created
    )
  }

  // 释放单个资源对：识别器和 Vad
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
    return SpeechRecognitionEngineError(code = code, message = message, cause = cause, recoverable = recoverable)
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
