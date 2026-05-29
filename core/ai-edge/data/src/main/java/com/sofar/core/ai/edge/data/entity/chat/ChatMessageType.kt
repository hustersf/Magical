package com.sofar.core.ai.edge.data.entity.chat

import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(ChatMessageType.TEXT, ChatMessageType.IMAGE, ChatMessageType.AUDIO)
annotation class ChatMessageType {
  companion object {
    const val TEXT = "text"
    const val IMAGE = "image"
    const val AUDIO = "audio"
  }
}