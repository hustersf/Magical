package com.k2fsa.sherpa.onnx

/**
 * 离线识别流。
 *
 * 对应 Native 层的 OfflineStream 对象，
 * 用于接收音频数据并参与识别。
 */
class OfflineStream internal constructor(
  private var ptr: Long,
) : AutoCloseable {

  internal val nativePtr: Long
    get() = ptr

  /**
   * 输入 PCM 音频数据。
   */
  fun acceptWaveform(
    samples: FloatArray,
    sampleRate: Int,
  ) {
    ensureValid()
    acceptWaveform(ptr, samples, sampleRate)
  }

  /**
   * 设置参数。
   */
  fun setOption(
    key: String,
    value: String,
  ) {
    ensureValid()
    setOption(ptr, key, value)
  }

  /**
   * 获取参数。
   */
  fun getOption(
    key: String,
  ): String {
    ensureValid()
    return getOption(ptr, key)
  }

  /**
   * 释放 Native 资源。
   */
  override fun close() {
    if (ptr == 0L) return

    delete(ptr)
    ptr = 0L
  }

  fun release() = close()

  private fun ensureValid() {
    check(ptr != 0L) {
      "OfflineStream has already been released."
    }
  }

  // ================= JNI =================

  private external fun acceptWaveform(
    ptr: Long,
    samples: FloatArray,
    sampleRate: Int,
  )

  private external fun setOption(
    ptr: Long,
    key: String,
    value: String,
  )

  private external fun getOption(
    ptr: Long,
    key: String,
  ): String

  private external fun delete(
    ptr: Long,
  )
}