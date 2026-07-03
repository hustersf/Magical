package com.k2fsa.sherpa.onnx

/**
 * 同音词替换配置。
 */
data class HomophoneReplacerConfig(
  var lexicon: String = "",
  var ruleFsts: String = "",
)