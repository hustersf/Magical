package com.sofar.feature.ai.edge.chat.api

import android.content.Context

interface ChatNavigator {
  fun launchChatDetail(
    context: Context,
    sessionId: String? = null,
    agentId: String? = null
  )
}