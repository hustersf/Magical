package com.sofar.core.speech

import android.content.Context
import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEvent
import com.sofar.core.speech.internal.engine.SpeechRecognitionEngineFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SpeechRecognitionController(
  context: Context,
  private val coroutineScope: CoroutineScope,
  private val listener: Listener,
  private val config: Config = Config(),
) {
  private val recognitionEngine: SpeechRecognitionEngine =
    SpeechRecognitionEngineFactory(context.applicationContext).create(config.engineType)

  data class Config(
    val languageCode: String? = null,
    val preferOffline: Boolean = true,
    val engineType: SpeechEngineType = SpeechEngineType.Android,
    val sampleRate: Int = 16_000,
    val modelId: String? = null,
    val restartDelayMillis: Long = DEFAULT_RESTART_DELAY_MILLIS,
    val finalizeDelayMillis: Long = DEFAULT_FINALIZE_DELAY_MILLIS,
  )

  interface Listener {
    fun onSessionStarted() = Unit
    fun onRecognizedTextChanged(text: String) = Unit
    fun onAudioLevelChanged(rmsDB: Float) = Unit
    fun onSpeechError(code: Int, hasRecognizedText: Boolean) = Unit
    fun onSessionCompleted(text: String) = Unit
    fun onSessionCanceled() = Unit
  }

  private var recognitionLoopJob: Job? = null
  private var completionDelayJob: Job? = null
  private var recognizedSegments = mutableListOf<String>()
  private var pendingPartialText: String = ""

  var isRecording: Boolean = false
    private set

  var isSessionActive: Boolean = false
    private set

  fun start() {
    if (isSessionActive) {
      Log.d(TAG, "start ignored: session already active")
      return
    }
    Log.d(TAG, "start")
    recognizedSegments.clear()
    pendingPartialText = ""
    isRecording = true
    isSessionActive = true
    completionDelayJob?.cancel()
    listener.onSessionStarted()
    listener.onRecognizedTextChanged(buildText())
    startSpeechRecognitionLoop()
  }

  fun stop() {
    if (!isSessionActive) {
      Log.d(TAG, "stop ignored: no active session")
      return
    }
    Log.d(TAG, "stop(isRecording=$isRecording, isSessionActive=$isSessionActive)")
    isRecording = false
    recognitionEngine.stop()
    completionDelayJob?.cancel()
    completionDelayJob = coroutineScope.launch {
      delay(config.finalizeDelayMillis)
      completeIfActive()
    }
  }

  fun cancel() {
    val hadActiveSession = isSessionActive
    Log.d(TAG, "cancel(hadActiveSession=$hadActiveSession)")
    recognitionEngine.cancel()
    clearSession()
    if (hadActiveSession) {
      listener.onSessionCanceled()
    }
  }

  fun release() {
    Log.d(TAG, "release")
    recognitionEngine.cancel()
    clearSession()
    recognitionEngine.release()
  }

  private fun clearSession() {
    recognitionLoopJob?.cancel()
    completionDelayJob?.cancel()
    resetSessionState()
  }

  private fun startSpeechRecognitionLoop() {
    recognitionLoopJob?.cancel()
    recognitionLoopJob = coroutineScope.launch {
      var shouldContinueListening = false
      var hasFinalResultInCurrentRecognition = false
      recognitionEngine.start(
        SpeechRecognitionConfig(
          languageCode = config.languageCode,
          sampleRate = config.sampleRate,
          preferOffline = config.preferOffline,
          enablePartialResult = true,
          modelId = config.modelId,
        )
      ).collect { result ->
        when (result) {
          SpeechRecognitionEvent.Ready,
          SpeechRecognitionEvent.Started -> {
            listener.onRecognizedTextChanged(buildText())
          }

          is SpeechRecognitionEvent.Text -> {
            if (result.isFinal) {
              appendFinalText(result.text)
              pendingPartialText = ""
              hasFinalResultInCurrentRecognition = true
              listener.onRecognizedTextChanged(buildText())
              if (isRecording) {
                shouldContinueListening = true
              } else {
                completeIfActive()
              }
            } else if (!hasFinalResultInCurrentRecognition) {
              pendingPartialText = result.text.trim()
              listener.onRecognizedTextChanged(buildText())
            }
          }

          is SpeechRecognitionEvent.Volume -> {
            listener.onAudioLevelChanged(result.rmsDB)
          }

          is SpeechRecognitionEvent.Error -> {
            shouldContinueListening = isRecording && isSessionActive && result.error.recoverable
            listener.onSpeechError(result.error.code, hasRecognizedText())
            if (!shouldContinueListening && isSessionActive && !hasRecognizedText()) {
              completeIfActive()
            }
          }

          SpeechRecognitionEvent.End -> {
            // End of speech, waiting for final result or error.
          }
        }
      }

      if (shouldContinueListening && isRecording && isSessionActive) {
        delay(config.restartDelayMillis)
        recognitionLoopJob = null
        startSpeechRecognitionLoop()
      }
    }
  }

  private fun completeIfActive() {
    if (!isSessionActive) return
    appendPendingPartialText()
    val finalText = recognizedSegments.joinToString(separator = SEPARATOR)
    Log.d(TAG, "completeIfActive(text='$finalText', segments=${recognizedSegments.size})")
    resetSessionState()
    listener.onSessionCompleted(finalText)
  }

  private fun resetSessionState() {
    isRecording = false
    isSessionActive = false
    pendingPartialText = ""
    recognizedSegments.clear()
    completionDelayJob?.cancel()
    recognitionLoopJob = null
  }


  private fun appendFinalText(text: String) {
    val normalizedText = text.trim()
    if (normalizedText.isNotEmpty()) {
      recognizedSegments += normalizedText
    }
  }

  private fun appendPendingPartialText() {
    appendFinalText(pendingPartialText)
    pendingPartialText = ""
  }

  private fun buildText(): String {
    return (recognizedSegments + pendingPartialText.trim())
      .filter { it.isNotEmpty() }
      .joinToString(separator = SEPARATOR)
  }

  private fun hasRecognizedText(): Boolean {
    return recognizedSegments.isNotEmpty() || pendingPartialText.isNotBlank()
  }

  private companion object {
    private const val TAG = "SpeechRecognitionController"
    private const val DEFAULT_RESTART_DELAY_MILLIS = 180L
    private const val DEFAULT_FINALIZE_DELAY_MILLIS = 900L
    private const val SEPARATOR = "  ·  "
  }
}
