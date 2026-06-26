package com.sofar.core.speech

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PressToTalkSpeechController(
  context: Context,
  private val coroutineScope: CoroutineScope,
  private val listener: Listener,
  private val config: Config = Config(),
) {

  private val speechHelper = SpeechToTextHelper(context.applicationContext)

  data class Config(
    val languageCode: String? = null,
    val preferOnDevice: Boolean = false,
    val restartDelayMillis: Long = DEFAULT_RESTART_DELAY_MILLIS,
    val finalizeDelayMillis: Long = DEFAULT_FINALIZE_DELAY_MILLIS,
  )

  interface Listener {
    fun onSessionStarted() = Unit
    fun onTextChanged(text: String) = Unit
    fun onRmsChanged(rmsdB: Float) = Unit
    fun onCancelingChanged(canceling: Boolean) = Unit
    fun onRecognitionError(code: Int, hasRecognizedText: Boolean) = Unit
    fun onCompleted(text: String) = Unit
    fun onCanceled() = Unit
  }

  private var speechJob: Job? = null
  private var finalizeJob: Job? = null
  private val recognizedSegments = mutableListOf<String>()
  private var pendingPartialText: String = ""
  private var preferOnDeviceInCurrentSession: Boolean = config.preferOnDevice

  var isRecording: Boolean = false
    private set

  var isSessionActive: Boolean = false
    private set

  var isCanceling: Boolean = false
    private set

  fun start() {
    Log.d(TAG, "start")
    recognizedSegments.clear()
    pendingPartialText = ""
    preferOnDeviceInCurrentSession = config.preferOnDevice
    isRecording = true
    isSessionActive = true
    isCanceling = false
    finalizeJob?.cancel()
    listener.onSessionStarted()
    listener.onTextChanged(buildText())
    startSpeechRecognitionLoop()
  }

  fun updateCanceling(canceling: Boolean) {
    if (isCanceling == canceling) return
    isCanceling = canceling
    Log.d(TAG, "updateCanceling(canceling=$canceling)")
    listener.onCancelingChanged(canceling)
  }

  fun stop() {
    Log.d(TAG, "stop(isRecording=$isRecording, isSessionActive=$isSessionActive)")
    isRecording = false
    speechHelper.stopListening()
    finalizeJob?.cancel()
    finalizeJob = coroutineScope.launch {
      delay(config.finalizeDelayMillis)
      completeIfActive()
    }
  }

  fun cancel() {
    Log.d(TAG, "cancel")
    speechHelper.cancelListening()
    speechJob?.cancel()
    finalizeJob?.cancel()
    resetSessionState()
    listener.onCanceled()
  }

  private fun startSpeechRecognitionLoop() {
    Log.d(TAG, "startSpeechRecognitionLoop(preferOnDevice=$preferOnDeviceInCurrentSession)")
    speechJob?.cancel()
    speechJob = coroutineScope.launch {
      var shouldContinueListening = false
      var hasFinalResultInCurrentRecognition = false
      speechHelper.startListening(
        languageCode = config.languageCode,
        preferOnDevice = preferOnDeviceInCurrentSession,
      ).collect { result ->
        when (result) {
          is SpeechResult.Ready -> {
            listener.onTextChanged(buildText())
          }

          is SpeechResult.Text -> {
            if (result.isFinal) {
              appendFinalText(result.text)
              pendingPartialText = ""
              hasFinalResultInCurrentRecognition = true
              listener.onTextChanged(buildText())
              if (isRecording) {
                shouldContinueListening = true
              } else {
                completeIfActive()
              }
            } else if (!hasFinalResultInCurrentRecognition) {
              pendingPartialText = result.text.trim()
              listener.onTextChanged(buildText())
            }
          }

          is SpeechResult.RmsChanged -> {
            listener.onRmsChanged(result.rmsdB)
          }

          is SpeechResult.Error -> {
            if (preferOnDeviceInCurrentSession && result.isOnDeviceLanguageError()) {
              Log.w(
                TAG,
                "On-device language unavailable, fallback to default recognizer. code=${result.code}"
              )
              preferOnDeviceInCurrentSession = false
            }
            shouldContinueListening = isRecording && isSessionActive
            listener.onRecognitionError(result.code, hasRecognizedText())
            if (!shouldContinueListening && isSessionActive && !hasRecognizedText()) {
              completeIfActive()
            }
          }

          is SpeechResult.End -> {
            // End of speech, waiting for final result or error.
          }
        }
      }

      if (shouldContinueListening && isRecording && isSessionActive) {
        delay(config.restartDelayMillis)
        speechJob = null
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
    listener.onCompleted(finalText)
  }

  private fun resetSessionState() {
    isRecording = false
    isSessionActive = false
    isCanceling = false
    pendingPartialText = ""
    recognizedSegments.clear()
    finalizeJob?.cancel()
  }

  private fun appendFinalText(text: String) {
    val normalizedText = text.trim()
    if (normalizedText.isNotEmpty()) {
      recognizedSegments += normalizedText
      Log.d(TAG, "appendFinalText(text='$normalizedText')")
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

  private fun SpeechResult.Error.isOnDeviceLanguageError(): Boolean {
    return code == SPEECH_ERROR_LANGUAGE_NOT_SUPPORTED || code == SPEECH_ERROR_LANGUAGE_UNAVAILABLE
  }

  private companion object {
    private const val TAG = "PressToTalkSpeechController"
    private const val DEFAULT_RESTART_DELAY_MILLIS = 180L
    private const val DEFAULT_FINALIZE_DELAY_MILLIS = 900L
    private const val SPEECH_ERROR_LANGUAGE_NOT_SUPPORTED = 12
    private const val SPEECH_ERROR_LANGUAGE_UNAVAILABLE = 13
    private const val SEPARATOR = "  ·  "
  }
}