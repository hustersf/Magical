package com.k2fsa.sherpa.onnx

/**
 * 离线识别结果。
 */
class OfflineRecognizerResult(
  val text: String,
  val tokens: Array<String>,
  val timestamps: FloatArray,
  val lang: String,
  val emotion: String,
  val event: String,
  val durations: FloatArray,
)