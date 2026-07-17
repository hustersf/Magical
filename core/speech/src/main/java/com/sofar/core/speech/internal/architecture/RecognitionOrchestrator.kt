package com.sofar.core.speech.internal.architecture

import android.content.Context
import android.util.Log
import com.sofar.core.speech.SpeechEngineType
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngine
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineConfig
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineError
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineEvent
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.internal.engine.SpeechRecognitionEngineFactory
import com.sofar.core.speech.internal.policy.RecognitionSessionPolicyFactory
import com.sofar.core.speech.internal.session.RecognitionSessionEvent
import com.sofar.core.speech.internal.session.RecognitionSessionOptions
import com.sofar.core.speech.internal.session.SpeechRecognitionSession
import com.sofar.core.speech.internal.transcript.RecognitionTranscriptAggregator
import com.sofar.core.speech.internal.transcript.TranscriptOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 核心编排器：整合状态机 + 注册表 + 用例 + 观测。
 *
 * 职责：
 * - 根据用例 + 当前状态计算转移
 * - 管理引擎/会话实例的生命周期
 * - 触发观测钩子
 * - 对外提供简洁的事件 Flow 接口
 */
internal class RecognitionOrchestrator(
  private val context: Context,
  private val coroutineScope: CoroutineScope,
  private val engineFactory: SpeechRecognitionEngineFactory = SpeechRecognitionEngineFactory(
    context.applicationContext
  ),
  private val sessionOptions: RecognitionSessionOptions = RecognitionSessionOptions(
    restartDelayMillis = 180L,
    finalizeDelayMillis = 900L,
  ),
  private val transcriptOptions: TranscriptOptions = TranscriptOptions(),
  private val telemetry: TelemetryHook = NoOpTelemetryHook(),
) {
  private val stateMachine = RecognitionStateMachine()

  // 会话级别实例（跨越单次 start/stop，引擎预热结果可复用）
  private var currentEngine: SpeechRecognitionEngine? = null
  private var currentEngineType: SpeechEngineType? = null
  private var currentSession: SpeechRecognitionSession? = null
  private var prepareJob: Job? = null
  private var nextSessionId: Long = 1L
  private var activeSessionId: Long = NO_ACTIVE_SESSION_ID


  private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 64)
  val events: SharedFlow<Any> = _events.asSharedFlow()

  init {
    stateMachine.addStateChangeListener { fromState, toState ->
      telemetry.onStateTransition(fromState, toState)
    }
  }

  /**
   * 统一用例入口：根据用例类型分发到对应处理方法
   */
  fun execute(useCase: RecognitionUseCase) {
    Log.d(
      TAG,
      "execute: useCase=${useCase::class.simpleName}, state=${stateMachine.getCurrentState()}"
    )
    when (useCase) {
      is RecognitionUseCase.PrepareEngine -> startPrepareIfNeeded(useCase.config)
      is RecognitionUseCase.StartRecognition -> coroutineScope.launch { start(useCase.config) }
      is RecognitionUseCase.StopRecognition -> stop()
      is RecognitionUseCase.CancelRecognition -> cancel()
      is RecognitionUseCase.Release -> release()
    }
  }

  // 阶段1：下载/验证模型 → 阶段2：引擎预热 → Ready（幂等）
  // 流程：Idle/ModelPreparationFailed → Preparing → ReadyToUse
  private suspend fun prepare(config: SpeechRecognitionEngineConfig) {
    val runningPrepareJob = currentCoroutineContext()[Job]
    val currentState = stateMachine.getCurrentState()
    Log.d(TAG, "prepare requested: engineType=${config.engineType}, state=$currentState")
    if (currentState is RecognitionState.ReadyToUse) {
      Log.d(TAG, "prepare skipped: engine already ready")
      publishEvent(SpeechRecognitionEngineModelEvent.Ready)
      return
    }

    if (currentState !is RecognitionState.Idle && currentState !is RecognitionState.ModelPreparationFailed) {
      Log.w(TAG, "prepare rejected: invalid state=$currentState")
      publishEvent(
        SpeechRecognitionEngineModelEvent.Error(
          SpeechRecognitionEngineError(
            code = ERROR_INVALID_STATE_FOR_PREPARE,
            message = "Invalid state for prepare: $currentState",
            recoverable = true,
          ),
        ),
      )
      return
    }

    stateMachine.transition(RecognitionEvent.PrepareModel)
    telemetry.onPrepareStart()

    try {
      val engine = obtainOrCreateEngine(config)
      Log.d(TAG, "prepare started: engine=${engine::class.java.simpleName}")
      engine.prepare().collect { event ->
        publishEvent(event)
        when (event) {
          is SpeechRecognitionEngineModelEvent.Ready -> {
            Log.d(TAG, "prepare completed")
            telemetry.onPrepareComplete()
            stateMachine.transition(RecognitionEvent.ModelPrepared)
          }

          is SpeechRecognitionEngineModelEvent.Error -> {
            Log.w(
              TAG,
              "prepare failed: code=${event.error.code}, msg=${event.error.message}"
            )
            telemetry.onPrepareFailed(event.error, event.error.recoverable)
            stateMachine.transition(RecognitionEvent.ModelPreparationError(event.error))
          }

          is SpeechRecognitionEngineModelEvent.Downloading -> {
            telemetry.onPrepareProgress(
              "downloading",
              event.progress,
            )
          }

          else -> Unit
        }
      }
    } catch (cancellation: CancellationException) {
      Log.d(TAG, "prepare canceled: ${cancellation.message}")
    } catch (e: Exception) {
      Log.w(TAG, "prepare failed", e)
      val error =
        SpeechRecognitionEngineError(
          code = ERROR_PREPARE_FAILED,
          message = e.message ?: "Unknown prepare error",
          cause = e,
          recoverable = true,
        )

      publishEvent(SpeechRecognitionEngineModelEvent.Error(error))
      telemetry.onPrepareFailed(error, true)
      stateMachine.transition(RecognitionEvent.ModelPreparationError(error))
    } finally {
      if (prepareJob === runningPrepareJob) {
        prepareJob = null
      }
    }
  }

  private suspend fun start(config: SpeechRecognitionEngineConfig) {
    val initialState = stateMachine.getCurrentState()
    Log.d(TAG, "start requested: engineType=${config.engineType}, state=$initialState")

    if (!canStartFrom(initialState)) {
      publishInvalidStartState(initialState)
      return
    }

    try {
      ensureReadyForStart(initialState, config)

      val engine =
        currentEngine ?: throw IllegalStateException("No engine available after prepare")
      telemetry.onRecognitionStart()
      val sessionId = nextSessionId++
      var createdSession: SpeechRecognitionSession? = null
      val session = SpeechRecognitionSession(
        engine = engine,
        request = config,
        policy = RecognitionSessionPolicyFactory.create(
          engine.capabilities,
        ),
        options = sessionOptions,
        transcriptAggregator =
          RecognitionTranscriptAggregator(
            transcriptOptions,
          ),
        coroutineScope = coroutineScope,
        eventSink = eventSink@{ sessionEvent ->
          if (sessionId != activeSessionId || currentSession !== createdSession) {
            Log.d(
              TAG,
              "ignore stale session event: sid=$sessionId, event=${sessionEvent::class.simpleName}"
            )
            return@eventSink
          }
          publishEvent(sessionEvent)
          when (sessionEvent) {
            is RecognitionSessionEvent.TextChanged -> {
              telemetry.onRecognitionPartial(sessionEvent.transcript.partialText)
              if (sessionEvent.transcript.text.isNotEmpty()) {
                telemetry.onRecognitionFinal(sessionEvent.transcript.text)
              }
            }

            is RecognitionSessionEvent.AudioLevelChanged -> {
              telemetry.onAudioVolumeChanged(sessionEvent.rmsDB)
            }

            is RecognitionSessionEvent.Error -> {
              Log.w(
                TAG,
                "recognition error: code=${sessionEvent.error.code}, msg=${sessionEvent.error.message}"
              )
              telemetry.onRecognitionFailed(sessionEvent.error)
            }

            is RecognitionSessionEvent.Completed -> {
              Log.d(
                TAG,
                "recognition completed: text='${sessionEvent.transcript.text}'"
              )
              clearCurrentSession()
              stateMachine.transition(RecognitionEvent.Completed)
            }

            RecognitionSessionEvent.Canceled -> {
              Log.d(TAG, "recognition canceled")
              clearCurrentSession()
              stateMachine.transition(RecognitionEvent.Cancel)
            }

            else -> Unit
          }
        },
      )

      createdSession = session
      currentSession = session
      activeSessionId = sessionId
      stateMachine.transition(RecognitionEvent.StartListening)
      session.start()
      Log.d(TAG, "recognition started: ${engine::class.java.simpleName}")
    } catch (e: Exception) {
      Log.w(TAG, "start failed", e)
      val error =
        SpeechRecognitionEngineError(
          code = ERROR_START_FAILED,
          message = e.message ?: "Unknown start error",
          cause = e,
        )

      publishEvent(SpeechRecognitionEngineEvent.Error(error))
      telemetry.onRecognitionFailed(error)
      stateMachine.transition(RecognitionEvent.ListeningError(error))
    }
  }

  private fun canStartFrom(state: RecognitionState): Boolean {
    return state is RecognitionState.ReadyToUse ||
        state is RecognitionState.Idle ||
        state is RecognitionState.Preparing
  }

  private fun publishInvalidStartState(state: RecognitionState) {
    Log.w(TAG, "start rejected: invalid state=$state")
    publishEvent(
      SpeechRecognitionEngineEvent.Error(
        SpeechRecognitionEngineError(
          code = ERROR_INVALID_STATE_FOR_START,
          message = "Invalid state for start: $state",
          recoverable = true,
        ),
      ),
    )
  }

  private suspend fun ensureReadyForStart(
    initialState: RecognitionState,
    config: SpeechRecognitionEngineConfig,
  ) {
    when (initialState) {
      RecognitionState.Idle -> {
        Log.d(TAG, "engine not ready, auto-preparing before start")
        startPrepareIfNeeded(config)
        awaitPreparationCompletedOrThrow()
      }

      RecognitionState.Preparing -> awaitPreparationCompletedOrThrow()

      else -> Unit
    }
  }

  private suspend fun awaitPreparationCompletedOrThrow() {
    prepareJob?.join()
    when (val state = stateMachine.getCurrentState()) {
      is RecognitionState.ReadyToUse -> return
      is RecognitionState.ModelPreparationFailed -> {
        throw IllegalStateException("Prepare failed before start: ${state.error.message}")
      }

      RecognitionState.Idle -> throw IllegalStateException("Prepare cancelled")
      RecognitionState.Released -> throw IllegalStateException("Cannot start after release")
      else -> throw IllegalStateException("Prepare interrupted, state=$state")
    }
  }

  private fun startPrepareIfNeeded(config: SpeechRecognitionEngineConfig) {
    val existing = prepareJob
    if (existing?.isActive == true) {
      Log.d(TAG, "prepare ignored: job already running")
      return
    }
    prepareJob = coroutineScope.launch { prepare(config) }
  }

  private fun obtainOrCreateEngine(config: SpeechRecognitionEngineConfig): SpeechRecognitionEngine {
    val expectedType = config.engineType
    currentEngine?.let { engine ->
      if (currentEngineType == expectedType) {
        Log.d(TAG, "engine reused: $expectedType")
        return engine
      }
      Log.d(TAG, "replace engine: $currentEngineType -> $expectedType")
      engine.release()
      currentEngine = null
      currentEngineType = null
    }

    return engineFactory.create(expectedType).also { engine ->
      currentEngine = engine
      currentEngineType = expectedType
      Log.d(TAG, "engine created: $expectedType")
    }
  }


  // 优雅停止识别：Listening → ReadyToUse（保留引擎，可直接再start）
  private fun stop() {
    Log.d(TAG, "stop")
    try {
      val handled = currentSession?.stop() == true
      if (!handled) {
        Log.d(TAG, "stop ignored: no active session")
      }
    } catch (e: Exception) {
      Log.w(TAG, "stop failed", e)
    }
  }

  // 立即中断识别：Listening → ReadyToUse（丢弃结果）
  private fun cancel() {
    Log.d(TAG, "cancel")
    try {
      val hasSession = currentSession != null
      currentSession?.cancel()
      if (!hasSession) {
        prepareJob?.cancel(CancellationException("Cancel during preparing"))
        prepareJob = null
        // Preparing 阶段尚未创建 session，主动补发取消事件，保证 UI 能关闭。
        publishEvent(RecognitionSessionEvent.Canceled)
        stateMachine.transition(RecognitionEvent.Cancel)
      }
    } catch (e: Exception) {
      Log.w(TAG, "cancel failed", e)
    }
  }

  // 清理所有原生资源：任意状态 → Released（最终态）
  private fun release() {
    Log.d(TAG, "release")
    try {
      prepareJob?.cancel(CancellationException("Release"))
      prepareJob = null
      clearCurrentSession()
      currentEngine?.release()
      currentEngine = null
      currentEngineType = null
      stateMachine.transition(RecognitionEvent.Release)
      telemetry.onResourceReleased()
    } catch (e: Exception) {
      Log.w(TAG, "release failed", e)
    }
  }

  // stop/cancel/release 共享：释放会话，保留引擎
  private fun clearCurrentSession() {
    currentSession?.release()
    currentSession = null
    activeSessionId = NO_ACTIVE_SESSION_ID
  }


  private fun publishEvent(event: Any) {
    if (!_events.tryEmit(event)) {
      Log.w(TAG, "event dropped: $event")
    }
  }


  private companion object {
    private const val TAG = "RecognitionOrchestrator"
    private const val NO_ACTIVE_SESSION_ID = -1L
    private const val ERROR_INVALID_STATE_FOR_PREPARE = 30_001
    private const val ERROR_PREPARE_FAILED = 30_002
    private const val ERROR_INVALID_STATE_FOR_START = 30_003
    private const val ERROR_START_FAILED = 30_004
  }
}