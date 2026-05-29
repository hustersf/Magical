package com.sofar.feature.ai.edge.chat.impl.detail

import com.sofar.core.ai.edge.database.entity.MessageEntity

data class ChatDetailUiState(
  val sessionTitle: String = "新对话",
  val messages: List<MessageEntity> = emptyList(), // 聊天气泡数据源
  val isAiThinking: Boolean = false,               // 控制底部 Loading 与发送键置灰
  val errorMessage: String? = null,                // 捕获端侧 OOM 或模型加载失败
)