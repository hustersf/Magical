package com.sofar.core.speech.internal.session

import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.policy.RecognitionSessionContext
import com.sofar.core.speech.internal.policy.RecognitionSessionPolicy
import com.sofar.core.speech.internal.transcript.RecognitionTranscript
import com.sofar.core.speech.internal.transcript.RecognitionTranscriptAggregator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal enum class SpeechRecognitionSessionState {
  Idle,
  Listening,
  Completing,
  Completed,
  Canceled,
  Released,
}

internal data class RecognitionSessionOptions(
  val restartDelayMillis: Long,
  val finalizeDelayMillis: Long,
)

internal sealed class RecognitionSessionEvent {
  data object Started : RecognitionSessionEvent()
  data class TextChanged(val transcript: RecognitionTranscript) : RecognitionSessionEvent()
  data class AudioLevelChanged(val rmsDB: Float) : RecognitionSessionEvent()
  data class Error(val error: SpeechRecognitionEngineError, val hasRecognizedText: Boolean) :
    RecognitionSessionEvent()

  data class Completed(val transcript: RecognitionTranscript) : RecognitionSessionEvent()
  data object Canceled : RecognitionSessionEvent()
}

internal class SpeechRecognitionSession(
  private val engine: SpeechRecognitionEngine,
  private val request: SpeechRecognitionEngineConfig,
  private val policy: RecognitionSessionPolicy,
  private val options: RecognitionSessionOptions,
  private val transcriptAggregator: RecognitionTranscriptAggregator,
  private val coroutineScope: CoroutineScope,
  private val eventSink: (RecognitionSessionEvent) -> Unit,
) {
  private var recognitionJob: Job? = null
  private var completionJob: Job? = null

  var state: SpeechRecognitionSessionState = SpeechRecognitionSessionState.Idle
    private set

  val isRecording: Boolean
    get() = state == SpeechRecognitionSessionState.Listening

  val isSessionActive: Boolean
    get() = state == SpeechRecognitionSessionState.Listening || state == SpeechRecognitionSessionState.Completing

  // 启动新一轮识别：重置聚合器 → Listening → 开启识别循环
  fun start(): Boolean {
    if (isSessionActive) {
      Log.d(TAG, "start ignored: session already active(state=$state)")
      return false
    }

    completionJob?.cancel()
    transcriptAggregator.reset()
    state = SpeechRecognitionSessionState.Listening
    eventSink(RecognitionSessionEvent.Started)
    recognitionJob = coroutineScope.launch { runRecognitionLoop() }
    return true
  }

  // 优雅停止：Listening → Completing → 等待 finalizeDelay 后 complete
  fun stop(): Boolean {
    if (!isSessionActive) {
      Log.d(TAG, "stop ignored: no active session(state=$state)")
      return false
    }

    Log.d(TAG, "stop(state=$state)")
    state = SpeechRecognitionSessionState.Completing
    engine.stop()
    completionJob?.cancel()
    completionJob = coroutineScope.launch {
      delay(options.finalizeDelayMillis)
      completeIfActive()
    }
    return true
  }

  // 立即取消：关闭引擎音频 + 取消 Job + 发送 Canceled 事件
  fun cancel(): Boolean {
    return cancelInternal(notify = true)
  }

  // 释放会话资源（标记为 Released，不发送取消事件）
  fun release() {
    cancelInternal(notify = false)
    state = SpeechRecognitionSessionState.Released
  }

  // 识别主循环：collectRecognitionOnce → 根据 policy 决定是否 restart → delay → 重复
  // policy 决定在识别最终结果或错误后是否重启下一轮
  private suspend fun runRecognitionLoop() {
    try {
      while (state == SpeechRecognitionSessionState.Listening) {
        val shouldRestart = collectRecognitionOnce()
        if (!shouldRestart || state != SpeechRecognitionSessionState.Listening) break
        delay(options.restartDelayMillis)
      }
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      Log.w(TAG, "recognition session failed", throwable)
    } finally {
      recognitionJob = null
    }
  }

  // 执行一轮识别：engine.start() → 处理事件 → 询问 policy 是否重启 → 返回 shouldRestart
  // 阶段：Ready → Started → Text(s) → [Volume] → Error/End
  private suspend fun collectRecognitionOnce(): Boolean {
    var shouldRestart = false
    var hasFinalResultInCurrentRecognition = false

    engine.start(request).collect { event ->
      when (event) {
        SpeechRecognitionEngineEvent.Ready,
        SpeechRecognitionEngineEvent.Started -> {
          eventSink(RecognitionSessionEvent.TextChanged(transcriptAggregator.transcript))
        }

        is SpeechRecognitionEngineEvent.Text -> {
          if (event.isFinal) {
            transcriptAggregator.onFinal(event.text)
            hasFinalResultInCurrentRecognition = true
            eventSink(RecognitionSessionEvent.TextChanged(transcriptAggregator.transcript))
            if (state == SpeechRecognitionSessionState.Listening) {
              shouldRestart = policy.shouldRestartAfterFinalResult(createPolicyContext())
            } else {
              completeIfActive()
            }
          } else if (!hasFinalResultInCurrentRecognition && state == SpeechRecognitionSessionState.Listening) {
            transcriptAggregator.onPartial(event.text)
            eventSink(RecognitionSessionEvent.TextChanged(transcriptAggregator.transcript))
          }
        }

        is SpeechRecognitionEngineEvent.Volume -> {
          eventSink(RecognitionSessionEvent.AudioLevelChanged(event.rmsDB))
        }

        is SpeechRecognitionEngineEvent.Error -> {
          shouldRestart = policy.shouldRetryAfterError(event.error, createPolicyContext())
          eventSink(
            RecognitionSessionEvent.Error(
              event.error,
              transcriptAggregator.transcript.hasText
            )
          )
          if (!shouldRestart && isSessionActive && !transcriptAggregator.transcript.hasText) {
            completeIfActive()
          }
        }

        SpeechRecognitionEngineEvent.End -> {
          // stop() 后若引擎已自然结束，则直接完成，避免额外等待 finalizeDelay。
          if (state == SpeechRecognitionSessionState.Completing) {
            completeIfActive()
          }
        }
      }
    }

    return shouldRestart
  }

  // 结束识别：完成聚合器 → Completed → 发送 Completed 事件
  private fun completeIfActive() {
    if (!isSessionActive) return
    val completedTranscript = transcriptAggregator.complete()
    Log.d(TAG, "completeIfActive(text='${completedTranscript.text}')")
    completionJob?.cancel()
    state = SpeechRecognitionSessionState.Completed
    eventSink(RecognitionSessionEvent.Completed(completedTranscript))
  }

  private fun cancelInternal(notify: Boolean): Boolean {
    val hadActiveSession = isSessionActive
    engine.cancel()
    recognitionJob?.cancel()
    completionJob?.cancel()
    transcriptAggregator.reset()
    state = SpeechRecognitionSessionState.Canceled
    if (notify && hadActiveSession) {
      eventSink(RecognitionSessionEvent.Canceled)
    }
    return hadActiveSession
  }

  private fun createPolicyContext(): RecognitionSessionContext {
    return RecognitionSessionContext(
      isRecording = state == SpeechRecognitionSessionState.Listening,
      isSessionActive = isSessionActive,
      hasRecognizedText = transcriptAggregator.transcript.hasText,
    )
  }

  private companion object {
    private const val TAG = "SpeechRecognitionSession"
  }
}