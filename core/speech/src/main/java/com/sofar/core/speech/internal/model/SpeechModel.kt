package com.sofar.core.speech.internal.model

import com.sofar.core.speech.sherpa.SherpaOnnxModelConfig

/**
 * A model descriptor owned by core:speech. It intentionally does not expose sherpa runtime objects.
 */
internal data class SpeechModel(
  val id: String,
  val name: String,
  val languageCode: String,
  val source: SpeechModelSource,
  val sherpaConfig: SherpaOnnxModelConfig,
)

internal sealed class SpeechModelSource {
  data class Assets(val assetDir: String) : SpeechModelSource()
  data class LocalFile(val dir: String) : SpeechModelSource()
  data class Downloaded(val dir: String) : SpeechModelSource()
}

internal data class ModelValidationResult(
  val isValid: Boolean,
  val missingFiles: List<String> = emptyList(),
  val message: String? = null,
)