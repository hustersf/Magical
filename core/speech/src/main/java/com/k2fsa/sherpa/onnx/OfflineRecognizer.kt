package com.k2fsa.sherpa.onnx

/**
 * Sherpa-ONNX 离线识别器。
 */
class OfflineRecognizer(
  val config: OfflineRecognizerConfig,
) : AutoCloseable {

  private var ptr: Long = newFromFile(config)

  /**
   * 创建识别流。
   */
  fun createStream(): OfflineStream {
    ensureValid()
    return OfflineStream(
      createStream(ptr),
    )
  }

  /**
   * 创建带热词的识别流。
   */
  fun createStream(
    hotwords: String,
  ): OfflineStream {
    ensureValid()
    return OfflineStream(
      createStreamWithHotwords(ptr, hotwords),
    )
  }

  /**
   * 执行解码。
   */
  fun decode(
    stream: OfflineStream,
  ) {
    ensureValid()
    decode(ptr, stream.nativePtr)
  }

  /**
   * 获取识别结果。
   */
  fun getResult(
    stream: OfflineStream,
  ): OfflineRecognizerResult {
    ensureValid()
    return getResult(
      stream.nativePtr,
    )
  }

  /**
   * 更新配置。
   */
  fun setConfig(
    config: OfflineRecognizerConfig,
  ) {
    ensureValid()
    setConfig(ptr, config,)
  }

  override fun close() {
    if (ptr == 0L) {
      return
    }

    delete(ptr)
    ptr = 0L
  }

  /**
   * 兼容原有调用方式。
   */
  fun release() = close()

  private fun ensureValid() {
    check(ptr != 0L) {
      "OfflineRecognizer has already been released."
    }
  }

  // ================= JNI =================

  private external fun delete(
    ptr: Long,
  )

  private external fun createStream(
    ptr: Long,
  ): Long

  private external fun createStreamWithHotwords(
    ptr: Long,
    hotwords: String,
  ): Long

  private external fun setConfig(
    ptr: Long,
    config: OfflineRecognizerConfig,
  )

  private external fun newFromFile(
    config: OfflineRecognizerConfig,
  ): Long

  private external fun decode(
    ptr: Long,
    streamPtr: Long,
  )

  private external fun getResult(
    streamPtr: Long,
  ): OfflineRecognizerResult

  companion object {

    /**
     * QNN 专用。
     */
    @JvmStatic
    external fun prependAdspLibraryPath(
      newPath: String,
    )
  }
}