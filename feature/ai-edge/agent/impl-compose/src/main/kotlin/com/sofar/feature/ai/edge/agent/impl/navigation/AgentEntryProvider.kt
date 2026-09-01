package com.sofar.feature.ai.edge.agent.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.agent.api.navigation.AgentNavKey
import com.sofar.feature.ai.edge.agent.impl.AgentScreen

fun EntryProviderScope<NavKey>.agentEntry(navigator: Navigator) {
  entry<AgentNavKey> {
    AgentScreen()
  }
}