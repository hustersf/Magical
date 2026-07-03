package com.k2fsa.sherpa.onnx

data class OfflineTransducerModelConfig(
  var encoder: String = "",
  var decoder: String = "",
  var joiner: String = "",
  var qnnConfig: QnnConfig = QnnConfig(),
)

data class OfflineParaformerModelConfig(
  var model: String = "",
  var qnnConfig: QnnConfig = QnnConfig(),
)

data class OfflineNemoEncDecCtcModelConfig(
  var model: String = "",
)

data class OfflineDolphinModelConfig(
  var model: String = "",
)

data class OfflineZipformerCtcModelConfig(
  var model: String = "",
  var qnnConfig: QnnConfig = QnnConfig(),
)

data class OfflineWenetCtcModelConfig(
  var model: String = "",
)

data class OfflineOmnilingualAsrCtcModelConfig(
  var model: String = "",
)

data class OfflineMedAsrCtcModelConfig(
  var model: String = "",
)

data class OfflineFireRedAsrCtcModelConfig(
  var model: String = "",
)

data class OfflineFireRedAsrModelConfig(
  var encoder: String = "",
  var decoder: String = "",
)

data class OfflineSenseVoiceModelConfig(
  var model: String = "",
  var language: String = "",
  var useInverseTextNormalization: Boolean = true,
  var qnnConfig: QnnConfig = QnnConfig(),
)

data class OfflineWhisperModelConfig(
  var encoder: String = "",
  var decoder: String = "",
  var language: String = "en",
  var task: String = "transcribe",
  var tailPaddings: Int = 1000,
  var enableTokenTimestamps: Boolean = false,
  var enableSegmentTimestamps: Boolean = false,
)

data class OfflineCanaryModelConfig(
  var encoder: String = "",
  var decoder: String = "",
  var srcLang: String = "en",
  var tgtLang: String = "en",
  var usePnc: Boolean = true,
)

data class OfflineCohereTranscribeModelConfig(
  var encoder: String = "",
  var decoder: String = "",
  var language: String = "",
  var usePunct: Boolean = true,
  var useItn: Boolean = true,
)

/**
 * Moonshine:
 *
 * v1:
 * preprocessor / encoder /
 * uncachedDecoder / cachedDecoder
 *
 * v2:
 * encoder / mergedDecoder
 */
data class OfflineMoonshineModelConfig(
  var preprocessor: String = "",
  var encoder: String = "",
  var uncachedDecoder: String = "",
  var cachedDecoder: String = "",
  var mergedDecoder: String = "",
)

data class OfflineModelConfig(

  // 模型
  var transducer: OfflineTransducerModelConfig = OfflineTransducerModelConfig(),
  var paraformer: OfflineParaformerModelConfig = OfflineParaformerModelConfig(),
  var whisper: OfflineWhisperModelConfig = OfflineWhisperModelConfig(),
  var senseVoice: OfflineSenseVoiceModelConfig = OfflineSenseVoiceModelConfig(),
  var fireRedAsr: OfflineFireRedAsrModelConfig = OfflineFireRedAsrModelConfig(),
  var fireRedAsrCtc: OfflineFireRedAsrCtcModelConfig = OfflineFireRedAsrCtcModelConfig(),
  var nemo: OfflineNemoEncDecCtcModelConfig = OfflineNemoEncDecCtcModelConfig(),
  var dolphin: OfflineDolphinModelConfig = OfflineDolphinModelConfig(),
  var zipformerCtc: OfflineZipformerCtcModelConfig = OfflineZipformerCtcModelConfig(),
  var wenetCtc: OfflineWenetCtcModelConfig = OfflineWenetCtcModelConfig(),
  var omnilingual: OfflineOmnilingualAsrCtcModelConfig = OfflineOmnilingualAsrCtcModelConfig(),
  var medasr: OfflineMedAsrCtcModelConfig = OfflineMedAsrCtcModelConfig(),
  var moonshine: OfflineMoonshineModelConfig = OfflineMoonshineModelConfig(),
  var canary: OfflineCanaryModelConfig = OfflineCanaryModelConfig(),
  var cohereTranscribe: OfflineCohereTranscribeModelConfig =
    OfflineCohereTranscribeModelConfig(),

  // 通用配置
  var teleSpeech: String = "",
  var tokens: String = "",
  var modelingUnit: String = "",
  var bpeVocab: String = "",

  var provider: String = "cpu",
  var modelType: String = "",

  var numThreads: Int = 1,
  var debug: Boolean = false,
)