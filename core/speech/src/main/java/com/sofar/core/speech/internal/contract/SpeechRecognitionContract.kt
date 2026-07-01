package com.sofar.core.speech.internal.contract

import kotlinx.coroutines.flow.Flow

// Naming convention: use `Recognition` terminology consistently.

/**
 * Internal recognition config shared by engine implementations.
 */
internal data class SpeechRecognitionConfig(
  val languageCode: String? = null,
  val sampleRate: Int = 16_000,
  val preferOffline: Boolean = true,
  val enablePartialResult: Boolean = true,
  val modelId: String? = null,
)

/**
 * Internal event model shared by Android and sherpa-onnx speech engines.
 */
internal sealed class SpeechRecognitionEvent {
  data object Ready : SpeechRecognitionEvent()
  data object Started : SpeechRecognitionEvent()
  data object End : SpeechRecognitionEvent()

  data class Text(
    val text: String,
    val isFinal: Boolean,
  ) : SpeechRecognitionEvent()

  data class Volume(
    val rmsDB: Float,
  ) : SpeechRecognitionEvent()

  data class Error(
    val error: SpeechRecognitionError,
  ) : SpeechRecognitionEvent()
}

internal data class SpeechRecognitionError(
  val code: Int,
  val message: String,
  val cause: Throwable? = null,
  val recoverable: Boolean = true,
)

/**
 * Common interface for all speech recognition implementations.
 */
internal interface SpeechRecognitionEngine {
  fun start(config: SpeechRecognitionConfig = SpeechRecognitionConfig()): Flow<SpeechRecognitionEvent>
  fun stop()
  fun cancel()
  fun release()
}