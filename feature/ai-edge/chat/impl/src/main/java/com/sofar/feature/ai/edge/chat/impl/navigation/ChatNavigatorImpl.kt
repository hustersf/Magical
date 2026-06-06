package com.sofar.feature.ai.edge.chat.impl.navigation

import android.content.Context
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
import com.sofar.feature.ai.edge.chat.api.ChatNavigator
import com.sofar.feature.ai.edge.chat.impl.detail.ChatDetailActivity
import javax.inject.Inject

class ChatNavigatorImpl @Inject constructor() : ChatNavigator {
  override fun launchChatDetail(
    context: Context,
    sessionId: String?,
    agentId: String?
  ) {
    ChatDetailActivity.launch(
      context = context,
      args = ChatDetailArgs(sessionId = sessionId, agentId = agentId)
    )
  }
}