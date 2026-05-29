package com.sofar.core.ai.edge.data.entity.chat

import androidx.annotation.StringDef

@Retention(AnnotationRetention.SOURCE)
@StringDef(ChatSessionType.TEXT, ChatSessionType.VISION, ChatSessionType.MEETING)
annotation class ChatSessionType {
  companion object {
    const val TEXT = "TEXT"       // 普通对话
    const val VISION = "VISION"   // 识图专属对话
    const val MEETING = "MEETING" // 会议专属对话
  }
}