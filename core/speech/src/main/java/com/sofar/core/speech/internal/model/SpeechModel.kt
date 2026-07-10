package com.sofar.core.speech.internal.model

import com.sofar.core.speech.sherpa.SherpaOnnxModelConfig

/**
 * A model descriptor owned by core:speech. It intentionally does not expose sherpa runtime objects.
 */
internal data class SpeechModel(
  val id: String,
  val name: String,
  val languageCode: String,
  val localDir: String,
  val sherpaConfig: SherpaOnnxModelConfig,
)


internal data class ModelValidationResult(
  val isValid: Boolean,
  val missingFiles: List<String> = emptyList(),
  val message: String? = null,
)