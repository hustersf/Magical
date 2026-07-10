package com.sofar.core.speech.android

import android.speech.SpeechRecognizer
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError

internal object AndroidSpeechErrorMapper {
  fun map(error: Int): SpeechRecognitionEngineError {
    return SpeechRecognitionEngineError(
      code = error,
      message = errorName(error),
      recoverable = error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
    )
  }

  fun errorName(error: Int): String {
    return when (error) {
      SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
      SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
      SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
      SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
      SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
      SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
      SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
      SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
      SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
      SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "ERROR_TOO_MANY_REQUESTS"
      SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "ERROR_SERVER_DISCONNECTED"
      SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "ERROR_LANGUAGE_NOT_SUPPORTED"
      SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "ERROR_LANGUAGE_UNAVAILABLE"
      SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> "ERROR_CANNOT_CHECK_SUPPORT"
      SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS -> "ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS"
      else -> "UNKNOWN"
    }
  }
}