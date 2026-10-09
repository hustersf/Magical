package com.sofar.feature.ai.edge.chat.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.chat.api.navigation.ChatDetailNavKey
import com.sofar.feature.ai.edge.chat.api.navigation.ChatNavKey
import com.sofar.feature.ai.edge.chat.impl.ChatDetailScreen
import com.sofar.feature.ai.edge.chat.impl.ChatHomeScreen

fun EntryProviderScope<NavKey>.chatEntry(navigator: Navigator) {
  entry<ChatNavKey> {
    ChatHomeScreen(navigator = navigator)
  }
  entry<ChatDetailNavKey> { key ->
    ChatDetailScreen(
      sessionId = key.sessionId,
      agentId = key.agentId,
      navigator = navigator
    )
  }
}
