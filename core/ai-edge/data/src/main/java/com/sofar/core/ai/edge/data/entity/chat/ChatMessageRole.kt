package com.sofar.core.ai.edge.data.entity.chat

import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(ChatMessageRole.USER, ChatMessageRole.ASSISTANT)
annotation class ChatMessageRole {
  companion object {
    const val USER = "user"
    const val ASSISTANT = "assistant"
  }
}