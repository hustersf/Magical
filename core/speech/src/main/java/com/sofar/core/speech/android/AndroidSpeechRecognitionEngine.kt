package com.sofar.core.speech.android

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineSessionMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

internal class AndroidSpeechRecognitionEngine(
  context: Context,
) : SpeechRecognitionEngine {

  override val capabilities: SpeechRecognitionEngineCapabilities =
    SpeechRecognitionEngineCapabilities(
      sessionMode = SpeechRecognitionEngineSessionMode.SingleUtterance,
      supportsPartialResult = true,
      supportsVolume = true,
      supportsOffline = true,
      supportsLanguageSwitching = true,
    )

  private val appContext = context.applicationContext
  private var speechRecognitionClient: SpeechRecognizer? = null
  private val unsupportedOfflineLanguages = mutableSetOf<String>()

  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> =
    flow {
      val startConfig = config.withOfflineFallbackIfUnsupported()
      var shouldFallbackToDefaultRecognitionClient = false
      startOnce(startConfig).collect { event ->
        if (event is SpeechRecognitionEngineEvent.Error && shouldFallbackToDefaultRecognitionClient(
            startConfig,
            event.error.code
          )
        ) {
          rememberUnsupportedOfflineLanguage(startConfig.languageCode)
          Log.w(
            TAG,
            "offline recognition does not support language=${startConfig.languageCode}, fallback to default recognizer"
          )
          shouldFallbackToDefaultRecognitionClient = true
        } else {
          emit(event)
        }
      }

      if (shouldFallbackToDefaultRecognitionClient) {
        releaseRecognitionClient()
        startOnce(config.copy(preferOffline = false)).collect { emit(it) }
      }
    }

  private fun startOnce(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> =
    callbackFlow {
      Log.d(TAG, "startOnce(config=$config)")
      if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
        trySend(SpeechRecognitionEngineEvent.Error(AndroidSpeechErrorMapper.map(SpeechRecognizer.ERROR_CLIENT)))
        close()
        return@callbackFlow
      }

      speechRecognitionClient = createRecognitionClient(config.preferOffline)

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, config.enablePartialResult)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, config.preferOffline)
        putExtra(
          RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
          COMPLETE_SILENCE_LENGTH_MILLIS
        )
        putExtra(
          RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
          POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS
        )
        putExtra(
          RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
          MINIMUM_SPEECH_LENGTH_MILLIS
        )
        config.languageCode?.let { putExtra(RecognizerIntent.EXTRA_LANGUAGE, it) }
      }

      val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          trySend(SpeechRecognitionEngineEvent.Ready)
        }

        override fun onBeginningOfSpeech() {
          trySend(SpeechRecognitionEngineEvent.Started)
        }

        override fun onRmsChanged(rmsdB: Float) {
          trySend(SpeechRecognitionEngineEvent.Volume(rmsdB))
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
          trySend(SpeechRecognitionEngineEvent.End)
        }

        override fun onError(error: Int) {
          Log.w(TAG, "onError(code=$error, name=${AndroidSpeechErrorMapper.errorName(error)})")
          trySend(SpeechRecognitionEngineEvent.Error(AndroidSpeechErrorMapper.map(error)))
          close()
        }

        override fun onResults(results: Bundle?) {
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!matches.isNullOrEmpty()) {
            trySend(SpeechRecognitionEngineEvent.Text(matches[0], isFinal = true))
          }
          close()
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!matches.isNullOrEmpty()) {
            trySend(SpeechRecognitionEngineEvent.Text(matches[0], isFinal = false))
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
      }

      speechRecognitionClient?.setRecognitionListener(listener)
      speechRecognitionClient?.startListening(intent)

      awaitClose {
        releaseRecognitionClient()
      }
    }

  override fun stop() {
    speechRecognitionClient?.stopListening()
  }

  override fun cancel() {
    speechRecognitionClient?.cancel()
  }

  override fun release() {
    releaseRecognitionClient()
  }

  private fun createRecognitionClient(preferOffline: Boolean): SpeechRecognizer {
    if (preferOffline) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(
          appContext
        )
      ) {
        try {
          return SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
        } catch (e: UnsupportedOperationException) {
          Log.w(TAG, "createOnDeviceSpeechRecognizer failed, fallback to default", e)
        }
      }
    }
    return SpeechRecognizer.createSpeechRecognizer(appContext)
  }

  private fun shouldFallbackToDefaultRecognitionClient(
    config: SpeechRecognitionEngineConfig,
    error: Int
  ): Boolean {
    return config.preferOffline && when (error) {
      SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
      SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
      SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> true

      else -> false
    }
  }

  private fun SpeechRecognitionEngineConfig.withOfflineFallbackIfUnsupported(): SpeechRecognitionEngineConfig {
    if (!preferOffline || !isUnsupportedOfflineLanguage(languageCode)) return this
    Log.d(TAG, "skip offline recognizer for unsupported language=$languageCode")
    return copy(preferOffline = false)
  }

  private fun rememberUnsupportedOfflineLanguage(languageCode: String?) {
    synchronized(unsupportedOfflineLanguages) {
      unsupportedOfflineLanguages += languageKey(languageCode)
    }
  }

  private fun isUnsupportedOfflineLanguage(languageCode: String?): Boolean {
    return synchronized(unsupportedOfflineLanguages) {
      languageKey(languageCode) in unsupportedOfflineLanguages
    }
  }

  private fun languageKey(languageCode: String?): String {
    return languageCode?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: DEFAULT_LANGUAGE_KEY
  }

  private fun releaseRecognitionClient() {
    speechRecognitionClient?.destroy()
    speechRecognitionClient = null
  }

  private companion object {
    private const val TAG = "AndroidSpeechRecognitionEngine"
    private const val DEFAULT_LANGUAGE_KEY = "__default__"
    private const val COMPLETE_SILENCE_LENGTH_MILLIS = 60_000L
    private const val POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS = 60_000L
    private const val MINIMUM_SPEECH_LENGTH_MILLIS = 60_000L
  }
}