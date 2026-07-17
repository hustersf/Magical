package com.sofar.core.speech.internal.architecture

import android.util.Log

/**
 * 有限状态机核心：管理 speech recognition 状态转移 + 不变量验证。
 *
 * 设计特点：
 * - 所有转移是同步的、原子性的（通过 synchronized）
 * - 转移规则集中定义，不分散到业务逻辑
 * - 非法转移返回 Ignored，而非异常（避免奔溃，便于调试）
 * - 支持注册 listener 观察全局状态变化（用于日志、指标、重试）
 */
internal class RecognitionStateMachine(
  private var currentState: RecognitionState = RecognitionState.Idle,
) {
  private val stateLock = Any()
  private val listeners = mutableListOf<(RecognitionState, RecognitionState) -> Unit>()

  // 线程安全读取当前状态
  fun getCurrentState(): RecognitionState = synchronized(stateLock) { currentState }

  // 原子性转移：验证合法性 → 更新状态 → 通知listener
  // 非法转移返回 Ignored（非异常，便于调试）
  fun transition(event: RecognitionEvent): TransitionResult = synchronized(stateLock) {
    val fromState = currentState
    val (newState, result) = computeNextState(fromState, event)
    return when (result) {
      TransitionResult.Success -> {
        currentState = newState
        listeners.forEach { it(fromState, newState) }
        Log.d(TAG, "state transition: ${newState.name} ← $event")
        TransitionResult.Success
      }

      else -> result
    }
  }

  // 注册状态变化监听器
  fun addStateChangeListener(listener: (RecognitionState, RecognitionState) -> Unit) {
    synchronized(stateLock) {
      listeners.add(listener)
    }
  }

  // 状态转移规则表（集中管理，类似 FSM 转移表，易审查维护）
  private fun computeNextState(
    fromState: RecognitionState,
    event: RecognitionEvent,
  ): Pair<RecognitionState, TransitionResult> {
    return when (fromState) {
      // ===== Idle =====
      RecognitionState.Idle -> when (event) {
        RecognitionEvent.PrepareModel -> {
          RecognitionState.Preparing to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in Idle state")
        }
      }

      // ===== Preparing =====
      RecognitionState.Preparing -> when (event) {
        is RecognitionEvent.ModelPreparationError -> {
          val newState = RecognitionState.ModelPreparationFailed(event.error)
          newState to TransitionResult.Success
        }

        RecognitionEvent.ModelPrepared -> {
          val newState = RecognitionState.ReadyToUse
          newState to TransitionResult.Success
        }

        RecognitionEvent.Cancel -> {
          RecognitionState.Idle to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in Preparing state")
        }
      }

      // ===== ReadyToUse =====
      RecognitionState.ReadyToUse -> when (event) {
        RecognitionEvent.StartListening -> {
          val newState = RecognitionState.Listening
          newState to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        RecognitionEvent.PrepareModel -> {
          // 已就绪，prepare 再次调用视为幂等，忽略。
          fromState to TransitionResult.Ignored("Already prepared, prepare() is idempotent")
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in ReadyToUse state")
        }
      }

      // ===== ModelPreparationFailed =====
      is RecognitionState.ModelPreparationFailed -> when (event) {
        RecognitionEvent.PrepareModel -> {
          // 允许重试：从已失败态重新准备
          RecognitionState.Preparing to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in ModelPreparationFailed state")
        }
      }

      // ===== Listening =====
      RecognitionState.Listening -> when (event) {
        is RecognitionEvent.ListeningError -> {
          val newState = RecognitionState.RecognitionFailed(event.error)
          newState to TransitionResult.Success
        }

        RecognitionEvent.Completed -> {
          // 正常结束，释放 Session，保留 Engine，可直接再次 start()
          val newState = RecognitionState.ReadyToUse
          newState to TransitionResult.Success
        }

        RecognitionEvent.Cancel -> {
          // 用户取消，释放 Session，保留 Engine
          val newState = RecognitionState.ReadyToUse
          newState to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in Listening state")
        }
      }

      // ===== RecognitionFailed =====
      is RecognitionState.RecognitionFailed -> when (event) {
        RecognitionEvent.PrepareModel -> {
          // 识别失败后允许重新准备（刷新引擎）
          RecognitionState.Preparing to TransitionResult.Success
        }

        RecognitionEvent.Release -> {
          RecognitionState.Released to TransitionResult.Success
        }

        else -> {
          fromState to TransitionResult.Ignored("Event $event invalid in RecognitionFailed state")
        }
      }

      // ===== Released =====
      RecognitionState.Released -> {
        fromState to TransitionResult.Ignored("State is Released, no further transitions allowed")
      }
    }
  }

  private companion object {
    private const val TAG = "RecognitionStateMachine"
  }
}