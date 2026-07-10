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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Decorates an engine with transparent local model preparation.
 *
 * Controller and business callers keep a single speech API. This layer owns the policy:
 * prepare/download the default model first, then start recording and recognition.
 */
internal class ModelPreparingSpeechRecognitionEngine(
  private val delegate: SpeechRecognitionEngine,
  private val modelProvider: SpeechModelProvider,
  coroutineScope: CoroutineScope,
) : SpeechRecognitionEngine {

  override val capabilities: SpeechRecognitionEngineCapabilities
    get() = delegate.capabilities

  private val prefetchJob: Job = coroutineScope.launch {
    runCatching { modelProvider.ensureReady() }
      .onFailure { Log.w(TAG, "prefetch speech model failed", it) }
  }

  override fun prepare(): Flow<SpeechRecognitionEngineModelEvent> = flow {
    try {
      prefetchJob.join()
      if (modelProvider.isModelReady()) {
        emit(SpeechRecognitionEngineModelEvent.Ready)
        return@flow
      }
      modelProvider.ensureReady()
      emit(SpeechRecognitionEngineModelEvent.Ready)
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      Log.w(TAG, "prepare speech model failed", throwable)
      emit(SpeechRecognitionEngineModelEvent.Error(prepareModelError(throwable)))
      return@flow
    }
  }

  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> = flow {
    delegate.start(config).collect { emit(it) }
  }

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
