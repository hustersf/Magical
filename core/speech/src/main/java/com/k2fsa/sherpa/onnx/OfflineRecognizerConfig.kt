package com.k2fsa.sherpa.onnx

/**
 * 离线识别器配置。
 */
data class OfflineRecognizerConfig(
  var featConfig: FeatureConfig = FeatureConfig(),

  var modelConfig: OfflineModelConfig = OfflineModelConfig(),

  var hr: HomophoneReplacerConfig = HomophoneReplacerConfig(),
  /**
   * 可选：
   * - greedy_search
   * - modified_beam_search
   */
  var decodingMethod: String = "greedy_search",

  var maxActivePaths: Int = 4,

  var hotwordsFile: String = "",

  var hotwordsScore: Float = 1.5f,

  var ruleFsts: String = "",

  var ruleFars: String = "",

  var blankPenalty: Float = 0.0f,
)
