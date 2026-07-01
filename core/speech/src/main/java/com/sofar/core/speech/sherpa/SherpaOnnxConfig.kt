package com.sofar.core.speech.sherpa

internal data class SherpaOnnxConfig(
  val sampleRate: Int = 16_000,
  val featureDim: Int = 80,
  val numThreads: Int = 2,
  val decodingMethod: String = "greedy_search",
  val provider: String = "cpu",
  val debug: Boolean = false,
  val enableEndpoint: Boolean = true,
  val rule1MinTrailingSilence: Float = 2.4f,
  val rule2MinTrailingSilence: Float = 1.2f,
  val rule3MinUtteranceLength: Float = 20.0f,
)

internal data class SherpaOnnxModelConfig(
  val modelType: SherpaOnnxModelType,
  val tokens: String,
  val encoder: String? = null,
  val decoder: String? = null,
  val joiner: String? = null,
  val model: String? = null,
) {
  fun requiredFiles(): List<String> {
    return buildList {
      add(tokens)
      when (modelType) {
        SherpaOnnxModelType.Transducer -> {
          addRequired(encoder, "encoder")
          addRequired(decoder, "decoder")
          addRequired(joiner, "joiner")
        }
        SherpaOnnxModelType.Paraformer,
        SherpaOnnxModelType.Zipformer2Ctc,
        SherpaOnnxModelType.Ctc -> {
          addRequired(model, "model")
        }
        SherpaOnnxModelType.Whisper -> {
          addRequired(encoder, "encoder")
          addRequired(decoder, "decoder")
        }
      }
    }
  }

  private fun MutableList<String>.addRequired(value: String?, name: String) {
    add(requireNotNull(value) { "$name is required for $modelType" })
  }
}

internal enum class SherpaOnnxModelType {
  Transducer,
  Paraformer,
  Zipformer2Ctc,
  Ctc,
  Whisper,
}
