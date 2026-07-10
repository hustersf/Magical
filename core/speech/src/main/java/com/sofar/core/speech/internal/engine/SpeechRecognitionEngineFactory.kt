package com.sofar.core.speech.internal.engine

import android.content.Context
import com.sofar.core.speech.SpeechEngineType
import com.sofar.core.speech.android.AndroidSpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineCapabilities
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineSessionMode
import com.sofar.core.speech.internal.model.DefaultSpeechModelProvider
import com.sofar.core.speech.sherpa.SherpaOnnxSpeechRecognitionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge

internal class SpeechRecognitionEngineFactory(
  private val context: Context,
  private val coroutineScope: CoroutineScope,
) {
  fun create(type: SpeechEngineType): SpeechRecognitionEngine {
    return when (type) {
      SpeechEngineType.Android -> AndroidSpeechRecognitionEngine(context.applicationContext)
      SpeechEngineType.SherpaOnnx -> createSherpaOnnxEngine()
      SpeechEngineType.Auto -> AutoSpeechRecognitionEngine(
        primary = createSherpaOnnxEngine(),
        fallback = AndroidSpeechRecognitionEngine(context.applicationContext),
      )
    }
  }

  private fun createSherpaOnnxEngine(): SpeechRecognitionEngine {
    val modelProvider = DefaultSpeechModelProvider(context.applicationContext)
    return ModelPreparingSpeechRecognitionEngine(
      delegate = SherpaOnnxSpeechRecognitionEngine(
        context = context.applicationContext,
        model = modelProvider.model,
      ),
      modelProvider = modelProvider,
      coroutineScope = coroutineScope,
    )
  }
}

internal class AutoSpeechRecognitionEngine(
  private val primary: SpeechRecognitionEngine?,
  private val fallback: SpeechRecognitionEngine,
) : SpeechRecognitionEngine {

  override val capabilities: SpeechRecognitionEngineCapabilities
    get() {
      val primaryCapabilities = primary?.capabilities
      val fallbackCapabilities = fallback.capabilities
      val sessionMode = if (primaryCapabilities?.sessionMode == SpeechRecognitionEngineSessionMode.Continuous &&
        fallbackCapabilities.sessionMode == SpeechRecognitionEngineSessionMode.Continuous
      ) {
        SpeechRecognitionEngineSessionMode.Continuous
      } else {
        SpeechRecognitionEngineSessionMode.SingleUtterance
      }
      return SpeechRecognitionEngineCapabilities(
        sessionMode = sessionMode,
        supportsPartialResult = primaryCapabilities?.supportsPartialResult ?: fallbackCapabilities.supportsPartialResult,
        supportsVolume = primaryCapabilities?.supportsVolume ?: fallbackCapabilities.supportsVolume,
        supportsOffline = primaryCapabilities?.supportsOffline ?: fallbackCapabilities.supportsOffline,
        supportsLanguageSwitching = primaryCapabilities?.supportsLanguageSwitching ?: fallbackCapabilities.supportsLanguageSwitching,
      )
    }

  override fun prepare(): Flow<SpeechRecognitionEngineModelEvent> {
    val primaryFlow = primary?.prepare()
    val fallbackFlow = fallback.prepare()

    // 💡 如果 primary 存在，把两个 Flow 编织在一起同时并发启动并向上传递
    return if (primaryFlow != null) {
      merge(primaryFlow, fallbackFlow)
    } else {
      fallbackFlow
    }
  }

  override fun start(config: SpeechRecognitionEngineConfig): Flow<SpeechRecognitionEngineEvent> = flow {
    val primaryEngine = primary
    if (primaryEngine == null) {
      fallback.start(config).collect { emit(it) }
      return@flow
    }

    var shouldFallback = false
    primaryEngine.start(config)
      .catch { shouldFallback = true }
      .collect { event ->
        if (event is SpeechRecognitionEngineEvent.Error && !event.error.recoverable) {
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