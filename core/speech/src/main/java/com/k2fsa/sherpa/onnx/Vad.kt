package com.k2fsa.sherpa.onnx

/**
 * Silero VAD 配置。
 */
data class SileroVadModelConfig(
  var model: String = "",
  var threshold: Float = 0.5f,
  var minSilenceDuration: Float = 0.25f,
  var minSpeechDuration: Float = 0.25f,
  var windowSize: Int = 512,
  var maxSpeechDuration: Float = 5.0f,
)

/**
 * TEN VAD 配置。
 */
data class TenVadModelConfig(
  var model: String = "",
  var threshold: Float = 0.5f,
  var minSilenceDuration: Float = 0.25f,
  var minSpeechDuration: Float = 0.25f,
  var windowSize: Int = 256,
  var maxSpeechDuration: Float = 5.0f,
)

/**
 * VAD 配置。
 */
data class VadModelConfig(
  var sileroVadModelConfig: SileroVadModelConfig =
    SileroVadModelConfig(),

  var tenVadModelConfig: TenVadModelConfig =
    TenVadModelConfig(),

  var sampleRate: Int = 16000,

  var numThreads: Int = 1,

  var provider: String = "cpu",

  var debug: Boolean = false,
)

/**
 * 语音片段。
 */
class SpeechSegment(
  val start: Int,
  val samples: FloatArray,
)

/**
 * 语音活动检测器。
 */
class Vad(
  val config: VadModelConfig,
) : AutoCloseable {

  private var ptr: Long = newFromFile(config)

  /**
   * 计算当前音频的语音概率。
   */
  fun compute(
    samples: FloatArray,
  ): Float {
    ensureValid()

    return compute(
      ptr,
      samples,
    )
  }

  /**
   * 输入音频数据。
   */
  fun acceptWaveform(
    samples: FloatArray,
  ) {
    ensureValid()

    acceptWaveform(
      ptr,
      samples,
    )
  }

  /**
   * 是否没有可读语音段。
   */
  fun empty(): Boolean {
    ensureValid()
    return empty(ptr)
  }

  /**
   * 移除当前语音段。
   */
  fun pop() {
    ensureValid()
    pop(ptr)
  }

  /**
   * 获取当前语音段。
   */
  fun front(): SpeechSegment {
    ensureValid()
    return front(ptr)
  }

  /**
   * 清空状态。
   */
  fun clear() {
    ensureValid()
    clear(ptr)
  }

  /**
   * 是否检测到语音。
   */
  fun isSpeechDetected(): Boolean {
    ensureValid()
    return isSpeechDetected(ptr)
  }

  /**
   * 重置检测器。
   */
  fun reset() {
    ensureValid()
    reset(ptr)
  }

  /**
   * 刷新剩余数据。
   */
  fun flush() {
    ensureValid()
    flush(ptr)
  }

  override fun close() {
    if (ptr == 0L) {
      return
    }

    delete(ptr)
    ptr = 0L
  }

  /**
   * 兼容旧版 API。
   */
  fun release() = close()

  private fun ensureValid() {
    check(ptr != 0L) {
      "Vad has already been released."
    }
  }

  // ================= JNI =================

  private external fun delete(
    ptr: Long,
  )

  private external fun newFromFile(
    config: VadModelConfig,
  ): Long

  private external fun acceptWaveform(
    ptr: Long,
    samples: FloatArray,
  )

  private external fun compute(
    ptr: Long,
    samples: FloatArray,
  ): Float

  private external fun empty(
    ptr: Long,
  ): Boolean

  private external fun pop(
    ptr: Long,
  )

  private external fun clear(
    ptr: Long,
  )

  private external fun front(
    ptr: Long,
  ): SpeechSegment

  private external fun isSpeechDetected(
    ptr: Long,
  ): Boolean

  private external fun reset(
    ptr: Long,
  )

  private external fun flush(
    ptr: Long,
  )
}