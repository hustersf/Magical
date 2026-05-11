package com.sofar.ai.edge.feature.chat.data

enum class ChatMessageType {
  INFO, WARNING, ERROR, TEXT, IMAGE, AUDIO_CLIP, LOADING, THINKING, SYSTEM
}

enum class ChatSide {
  USER, AGENT, SYSTEM
}

open class ChatMessage(
  open val type: ChatMessageType,
  open val side: ChatSide,
  open val latencyMs: Float = -1f
)

class ChatMessageText(
  var content: String,
  override val side: ChatSide,
  override val latencyMs: Float = 0f
) : ChatMessage(type = ChatMessageType.TEXT, side = side, latencyMs = latencyMs)

class ChatMessageLoading(
  val message: String = "AI 正在思考..."
) : ChatMessage(type = ChatMessageType.LOADING, side = ChatSide.AGENT)
