package com.sofar.core.speech.internal.policy

import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineSessionMode

internal data class RecognitionSessionContext(
  val isRecording: Boolean,
  val isSessionActive: Boolean,
  val hasRecognizedText: Boolean,
)

internal interface RecognitionSessionPolicy {
  fun shouldRestartAfterFinalResult(context: RecognitionSessionContext): Boolean
  fun shouldRetryAfterError(error: SpeechRecognitionEngineError, context: RecognitionSessionContext): Boolean
}

internal class SingleUtteranceRecognitionPolicy : RecognitionSessionPolicy {
  override fun shouldRestartAfterFinalResult(context: RecognitionSessionContext): Boolean {
    return context.isRecording && context.isSessionActive
  }

  override fun shouldRetryAfterError(error: SpeechRecognitionEngineError, context: RecognitionSessionContext): Boolean {
    return context.isRecording && context.isSessionActive && error.recoverable
  }
}

internal class ContinuousRecognitionPolicy : RecognitionSessionPolicy {
  override fun shouldRestartAfterFinalResult(context: RecognitionSessionContext): Boolean {
    return false
  }

  override fun shouldRetryAfterError(error: SpeechRecognitionEngineError, context: RecognitionSessionContext): Boolean {
    return context.isRecording && context.isSessionActive && error.recoverable
  }
}

internal object RecognitionSessionPolicyFactory {
  fun create(capabilities: SpeechRecognitionEngineCapabilities): RecognitionSessionPolicy {
    return when (capabilities.sessionMode) {
      SpeechRecognitionEngineSessionMode.SingleUtterance -> SingleUtteranceRecognitionPolicy()
      SpeechRecognitionEngineSessionMode.Continuous -> ContinuousRecognitionPolicy()
    }
  }
}