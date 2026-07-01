package com.sofar.core.speech.internal.engine

import android.content.Context
import com.sofar.core.speech.SpeechEngineType
import com.sofar.core.speech.android.AndroidSpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEvent
import com.sofar.core.speech.internal.model.SpeechModelManager
import com.sofar.core.speech.sherpa.SherpaOnnxSpeechRecognitionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class SpeechRecognitionEngineFactory(
  private val context: Context,
) {
  private val modelManager: SpeechModelManager = SpeechModelManager(context.applicationContext)

  fun create(type: SpeechEngineType): SpeechRecognitionEngine {
    return when (type) {
      SpeechEngineType.Android -> AndroidSpeechRecognitionEngine(context.applicationContext)
      SpeechEngineType.SherpaOnnx -> SherpaOnnxSpeechRecognitionEngine(
        context = context.applicationContext,
        modelManager = modelManager,
      )
      SpeechEngineType.Auto -> AutoSpeechRecognitionEngine(
        primary = if (modelManager.hasModels()) SherpaOnnxSpeechRecognitionEngine(context.applicationContext, modelManager) else null,
        fallback = AndroidSpeechRecognitionEngine(context.applicationContext),
      )
    }
  }
}

internal class AutoSpeechRecognitionEngine(
  private val primary: SpeechRecognitionEngine?,
  private val fallback: SpeechRecognitionEngine,
) : SpeechRecognitionEngine {

  override fun start(config: SpeechRecognitionConfig): Flow<SpeechRecognitionEvent> = flow {
    val primaryEngine = primary
    if (primaryEngine == null || config.modelId.isNullOrBlank()) {
      fallback.start(config).collect { emit(it) }
      return@flow
    }

    var shouldFallback = false
    primaryEngine.start(config)
      .catch { shouldFallback = true }
      .collect { event ->
        if (event is SpeechRecognitionEvent.Error && !event.error.recoverable) {
          shouldFallback = true
        } else {
          emit(event)
        }
      }

    if (shouldFallback) {
      fallback.start(config).collect { emit(it) }
    }
  }

  override fun stop() {
    primary?.stop()
    fallback.stop()
  }

  override fun cancel() {
    primary?.cancel()
    fallback.cancel()
  }

  override fun release() {
    primary?.release()
    fallback.release()
  }
}
