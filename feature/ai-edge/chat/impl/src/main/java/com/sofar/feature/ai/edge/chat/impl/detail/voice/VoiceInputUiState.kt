package com.sofar.feature.ai.edge.chat.impl.detail.voice

data class VoiceInputUiState(
  val isVoiceMode: Boolean = false,          // 当前是否是语音模式
  val isVoiceOverlayVisible: Boolean = false, // 语音浮层是否展示
  val isVoiceCanceling: Boolean = false,      // 语音输入是否处于上移取消态
  val voiceRecognizedText: String = "",       // 当前语音识别文本预览
  val voiceRmsDb: Float = 0f                  // 当前麦克风音量
)
