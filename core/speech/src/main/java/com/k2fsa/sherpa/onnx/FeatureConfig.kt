package com.k2fsa.sherpa.onnx

/**
 * 音频特征提取配置。
 */
data class FeatureConfig(

  /**
   * 音频采样率。
   *
   * 默认：
   * 16000 Hz
   */
  var sampleRate: Int = 16_000,

  /**
   * 特征维度。
   *
   * 常见：
   * 80
   */
  var featureDim: Int = 80,

  /**
   * Dither 参数。
   *
   * 通常保持 0。
   */
  var dither: Float = 0.0f,
)