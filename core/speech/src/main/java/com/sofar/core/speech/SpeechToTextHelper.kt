

package com.sofar.core.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class SpeechResult {
  data class Text(val text: String, val isFinal: Boolean) : SpeechResult()
  data class Error(val code: Int) : SpeechResult()
  data class RmsChanged(val rmsdB: Float) : SpeechResult()
  object Ready : SpeechResult()
  object End : SpeechResult()
}

class SpeechToTextHelper(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null

  /**
   * Start listening to speech from microphone and returns a Flow of [SpeechResult].
   * Caller needs to ensure RECORD_AUDIO permission is granted before calling this method.
   */
  fun startListening(
    languageCode: String? = null,
    preferOnDevice: Boolean = false,
  ): Flow<SpeechResult> = callbackFlow {
    Log.d(TAG, "startListening(languageCode=$languageCode, preferOnDevice=$preferOnDevice)")
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      Log.w(TAG, "Speech recognition is not available")
      trySend(SpeechResult.Error(SpeechRecognizer.ERROR_CLIENT))
      close()
      return@callbackFlow
    }

    speechRecognizer = createRecognizer(preferOnDevice)
    Log.d(TAG, "SpeechRecognizer created")

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOnDevice)
      putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, COMPLETE_SILENCE_LENGTH_MILLIS)
      putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS)
      putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, MINIMUM_SPEECH_LENGTH_MILLIS)
      if (languageCode != null) {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
      }
    }

    val listener = object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
        trySend(SpeechResult.Ready)
      }

      override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech")
      }

      override fun onRmsChanged(rmsdB: Float) {
        trySend(SpeechResult.RmsChanged(rmsdB))
      }

      override fun onBufferReceived(buffer: ByteArray?) {
        Log.v(TAG, "onBufferReceived(size=${buffer?.size ?: 0})")
      }

      override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech")
        trySend(SpeechResult.End)
      }

      override fun onError(error: Int) {
        Log.w(TAG, "onError(code=$error, name=${errorName(error)})")
        trySend(SpeechResult.Error(error))
        close()
      }

      override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(TAG, "onResults(matches=${matches.orEmpty()})")
        if (!matches.isNullOrEmpty()) {
          trySend(SpeechResult.Text(matches[0], isFinal = true))
        }
        close()
      }

      override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d(TAG, "onPartialResults(matches=${matches.orEmpty()})")
        if (!matches.isNullOrEmpty()) {
          trySend(SpeechResult.Text(matches[0], isFinal = false))
        }
      }

      override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d(TAG, "onEvent(eventType=$eventType)")
      }
    }

    speechRecognizer?.setRecognitionListener(listener)
    speechRecognizer?.startListening(intent)
    Log.d(TAG, "SpeechRecognizer.startListening called")

    awaitClose {
      Log.d(TAG, "callbackFlow awaitClose: destroy SpeechRecognizer")
      speechRecognizer?.destroy()
      speechRecognizer = null
    }
  }

  fun stopListening() {
    Log.d(TAG, "stopListening")
    speechRecognizer?.stopListening()
  }

  fun cancelListening() {
    Log.d(TAG, "cancelListening")
    speechRecognizer?.cancel()
  }

  private fun createRecognizer(preferOnDevice: Boolean): SpeechRecognizer {
    if (preferOnDevice) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Log.d(TAG, "On-device SpeechRecognizer requires API 31+, fallback to default")
        return SpeechRecognizer.createSpeechRecognizer(context)
      }
      val isOnDeviceAvailable = SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
      Log.d(TAG, "isOnDeviceRecognitionAvailable=$isOnDeviceAvailable")
      if (isOnDeviceAvailable) {
        try {
          Log.d(TAG, "createOnDeviceSpeechRecognizer")
          return SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } catch (e: UnsupportedOperationException) {
          Log.w(TAG, "createOnDeviceSpeechRecognizer failed, fallback to default", e)
        }
      }
    }
    Log.d(TAG, "createSpeechRecognizer")
    return SpeechRecognizer.createSpeechRecognizer(context)
  }

  private fun errorName(error: Int): String {
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

  private companion object {
    private const val TAG = "SpeechToTextHelper"
    private const val COMPLETE_SILENCE_LENGTH_MILLIS = 60_000L
    private const val POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS = 60_000L
    private const val MINIMUM_SPEECH_LENGTH_MILLIS = 60_000L
  }
}