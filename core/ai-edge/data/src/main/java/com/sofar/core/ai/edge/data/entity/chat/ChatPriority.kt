package com.sofar.core.ai.edge.data.entity.chat

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(ChatPriority.WORKSPACE, ChatPriority.PINNED, ChatPriority.NORMAL)
annotation class ChatPriority {
  companion object {
    const val WORKSPACE = 100 // 常驻顶部的官方工作区会话
    const val PINNED = 50     // 用户长按手动置顶的历史会话
    const val NORMAL = 0      // 遵循时间线倒序的普通历史会话
  }
}