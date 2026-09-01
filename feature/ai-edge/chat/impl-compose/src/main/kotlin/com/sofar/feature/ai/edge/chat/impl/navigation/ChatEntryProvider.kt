package com.sofar.feature.ai.edge.chat.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.chat.api.navigation.ChatNavKey
import com.sofar.feature.ai.edge.chat.impl.ChatScreen

fun EntryProviderScope<NavKey>.chatEntry(navigator: Navigator) {
  entry<ChatNavKey> {
    ChatScreen()
  }
}