package com.sofar.feature.ai.edge.chat.impl.detail.voice

data class VoiceInputUiState(
  val isVoiceMode: Boolean = false,           // 当前是否是语音模式
  val isVoiceOverlayVisible: Boolean = false, // 语音浮层是否展示
  val isVoiceEngineUsable: Boolean = false,   // 语音引擎是否已可识别（会话粒度，每次 start 后更新）
  val isVoiceCanceling: Boolean = false,      // 语音输入是否处于上移取消态
  val voiceRecognizedText: String = "",       // 当前语音识别文本预览
  val voiceRmsDB: Float = 0f,                 // 当前麦克风音量
  val speechEngineReady: Boolean = false,     // SherpaOnnx 离线引擎已就绪（模型粒度，全局一次性）
  /**
   * 模型下载/解压进度：
   *  -1 = 空闲（未下载或已就绪）
   *  -3 = 正在检查模型
   *  0..100 = 下载进度百分比
   *  -2 = 正在解压
   */
  val speechModelDownloadProgress: Int = SPEECH_MODEL_PROGRESS_IDLE,
) {
  companion object {
    const val SPEECH_MODEL_PROGRESS_IDLE = -1           // 空闲
    const val SPEECH_MODEL_PROGRESS_CHECKING = -3       // 检查中
    const val SPEECH_MODEL_PROGRESS_UNZIPPING = -2      // 解压中
    // 0..100 为下载进度百分比
  }
}