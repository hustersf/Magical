package com.sofar.core.speech.internal.engine

import android.util.Log
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.model.SpeechModelProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

/**
 * Decorates an engine with transparent local model preparation.
 *
 * Controller and business callers keep a single speech API. This layer owns the policy:
 * prepare/download the default model first, then start recording and recognition.
 */
internal class ModelPreparingSpeechRecognitionEngine(
  private val delegate: SpeechRecognitionEngine,
  private val modelProvider: SpeechModelProvider,
) : SpeechRecognitionEngine {

  override val capabilities: SpeechRecognitionEngineCapabilities
    get() = delegate.capabilities

  // 两阶段准备：
  // 阶段1（透出过程事件）: modelProvider.ensureReady() → Checking/Downloading/Unzipping
  // 阶段2（委托）: delegate.prepare() → Ready (含 JNI 初始化)
  override fun prepare(): Flow<SpeechRecognitionEngineModelEvent> = channelFlow {
    try {
      var modelReady = false
      // 阶段1：下载/校验模型，过程事件直接透出（Checking → Downloading → Unzipping）
      modelProvider.ensureReady { event ->
        when (event) {
          SpeechRecognitionEngineModelEvent.Ready -> {
            modelReady = true
          }

          else -> {
            trySend(event)
          }
        }
      }

      if (!modelReady) {
        trySend(
          SpeechRecognitionEngineModelEvent.Error(
            prepareModelError(IllegalStateException("Model provider completed without Ready event")),
          ),
        )
        return@channelFlow
      }

      // 阶段2：引擎预热（Sherpa recognizer/vad 初始化），最终 Ready 由 delegate 发出
      delegate.prepare().collect { event -> trySend(event) }
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      Log.w(TAG, "prepare speech model failed", throwable)
      trySend(SpeechRecognitionEngineModelEvent.Error(prepareModelError(throwable)))
    }
  }

  // 透传委托：不独立处理，直接转发至原引擎
  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> =
    channelFlow {
      try {
        delegate.start(config).collect { trySend(it) }
      } catch (cancellation: CancellationException) {
        throw cancellation
      }
    }

  // 透传委托：stop/cancel/release
  override fun stop() {
    delegate.stop()
  }

  override fun cancel() {
    delegate.cancel()
  }

  override fun release() {
    delegate.release()
  }

  private fun prepareModelError(cause: Throwable): SpeechRecognitionEngineError {
    return SpeechRecognitionEngineError(
      code = ERROR_PREPARE_MODEL,
      message = cause.message ?: "Prepare speech model failed",
      cause = cause,
      recoverable = false,
    )
  }

  private companion object {
    private const val TAG = "ModelPreparingSpeechRecognitionEngine"
    private const val ERROR_PREPARE_MODEL = 20_001
  }
}