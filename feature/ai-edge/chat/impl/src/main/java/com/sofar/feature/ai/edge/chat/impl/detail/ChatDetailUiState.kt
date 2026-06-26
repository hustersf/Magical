package com.sofar.feature.ai.edge.chat.impl.detail

import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageState
import com.sofar.feature.ai.edge.chat.impl.detail.voice.VoiceInputUiState

data class ChatDetailUiState(
  val sessionTitle: String = "",
  val messages: List<MessageEntity> = emptyList(),  // 聊天气泡数据源
  val isEngineLoading: Boolean = true,              // 模型正在初始化
  val isModelReady: Boolean = false,                // 模型是否初始化成功
  val isAiResponding: Boolean = false,              // 模型正在吐字中
  val currentStreamingText: String? = null,         // 当前大模型正在内存中高频蹦的字
  val selectedImages: List<SelectedImageState> = emptyList(),  //选中的本地图片
  val chatInputText: String = "",                   // 输入框文本
  val voiceState: VoiceInputUiState = VoiceInputUiState()  // 语音面板状态
)
