package com.sofar.core.speech.internal.contract

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Naming convention: use `Recognition` terminology consistently.

/**
 * Internal recognition config shared by engine implementations.
 */
internal data class SpeechRecognitionEngineConfig(
  val languageCode: String? = null,
  val sampleRate: Int = 16_000,
  val preferOffline: Boolean = true,
  val enablePartialResult: Boolean = true,
)

internal sealed class SpeechRecognitionEngineModelEvent {
  data object Checking : SpeechRecognitionEngineModelEvent()
  data class Downloading(val progress: Int) : SpeechRecognitionEngineModelEvent()
  data object Unzipping : SpeechRecognitionEngineModelEvent()
  data object Ready : SpeechRecognitionEngineModelEvent()
  data class Error(val error: SpeechRecognitionEngineError) : SpeechRecognitionEngineModelEvent()
}

/**
 * Internal event model shared by Android and sherpa-onnx speech engines.
 */
internal sealed class SpeechRecognitionEngineEvent {
  data object Ready : SpeechRecognitionEngineEvent()
  data object Started : SpeechRecognitionEngineEvent()
  data object End : SpeechRecognitionEngineEvent()

  data class Text(
    val text: String,
    val isFinal: Boolean,
  ) : SpeechRecognitionEngineEvent()

  data class Volume(
    val rmsDB: Float,
  ) : SpeechRecognitionEngineEvent()

  data class Error(
    val error: SpeechRecognitionEngineError,
  ) : SpeechRecognitionEngineEvent()
}

internal data class SpeechRecognitionEngineError(
  val code: Int,
  val message: String,
  val cause: Throwable? = null,
  val recoverable: Boolean = true,
)

/**
 * Describes how long a single [SpeechRecognitionEngine.start] call is expected to listen.
 */
internal enum class SpeechRecognitionEngineSessionMode {
  /**
   * The engine naturally completes after one utterance/result and needs controller-level restart
   * to implement long dictation.
   */
  SingleUtterance,

  /**
   * The engine owns a continuous audio/recognition pipeline until stop/cancel/release.
   */
  Continuous,
}

/**
 * Capability model used by session orchestration policies instead of concrete engine types.
 */
internal data class SpeechRecognitionEngineCapabilities(
  val sessionMode: SpeechRecognitionEngineSessionMode = SpeechRecognitionEngineSessionMode.SingleUtterance,
  val supportsPartialResult: Boolean = true,
  val supportsVolume: Boolean = true,
  val supportsOffline: Boolean = true,
  val supportsLanguageSwitching: Boolean = true,
)

/**
 * Common interface for all speech recognition implementations.
 */
internal interface SpeechRecognitionEngine {
  val capabilities: SpeechRecognitionEngineCapabilities
    get() = SpeechRecognitionEngineCapabilities()

  val sessionMode: SpeechRecognitionEngineSessionMode
    get() = capabilities.sessionMode

  fun prepare(): Flow<SpeechRecognitionEngineModelEvent> = flow {
    emit(SpeechRecognitionEngineModelEvent.Ready)
  }

  fun start(config: SpeechRecognitionEngineConfig = SpeechRecognitionEngineConfig()): Flow<SpeechRecognitionEngineEvent>
  fun stop()
  fun cancel()
  fun release()
}