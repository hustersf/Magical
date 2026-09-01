package com.sofar.ai.edge.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.sofar.core.ai.edge.design.theme.AppTheme
import com.sofar.core.ai.edge.navigation.NavigationState
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.core.ai.edge.navigation.rememberNavigationState
import com.sofar.feature.ai.edge.agent.api.navigation.AgentNavKey
import com.sofar.feature.ai.edge.agent.impl.navigation.agentEntry
import com.sofar.feature.ai.edge.chat.api.navigation.ChatNavKey
import com.sofar.feature.ai.edge.chat.impl.navigation.chatEntry
import com.sofar.feature.ai.edge.explore.api.navigation.ExploreNavKey
import com.sofar.feature.ai.edge.explore.impl.navigation.exploreEntry
import com.sofar.feature.ai.edge.models.api.navigation.ModelsNavKey
import com.sofar.feature.ai.edge.models.impl.navigation.modelsEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AppTheme(dynamicColor = false) {
        val navigationState = rememberNavigationState(
          startKey = ChatNavKey,
          topLevelKeys = setOf(
            ChatNavKey,
            AgentNavKey,
            ExploreNavKey,
            ModelsNavKey
          )
        )
        val navigator = remember(navigationState) { Navigator(navigationState) }
        MainScreen(navigationState, navigator)
      }
    }
  }
}

@Composable
fun MainScreen(navigationState: NavigationState, navigator: Navigator) {
  Scaffold(
    bottomBar = {
      NavigationBar {
        val currentTopLevelKey = navigationState.currentTopLevelKey
        navigationState.topLevelKeys.forEach { key ->
          NavigationBarItem(
            icon = { Icon(getIconForKey(key), contentDescription = null) },
            label = { Text(getLabelForKey(key)) },
            selected = currentTopLevelKey == key,
            onClick = { navigator.navigate(key) }
          )
        }
      }
    }
  ) { innerPadding ->
    val entryProvider = remember(navigator) {
      entryProvider<NavKey> {
        chatEntry(navigator)
        agentEntry(navigator)
        exploreEntry(navigator)
        modelsEntry(navigator)
      }
    }

    NavDisplay(
      modifier = Modifier.padding(innerPadding),
      backStack = navigationState.currentSubStack,
      onBack = { navigator.goBack() },
      entryProvider = entryProvider
    )
  }
}

fun getIconForKey(key: NavKey): ImageVector {
  return when (key) {
    ChatNavKey -> Icons.AutoMirrored.Filled.Chat
    AgentNavKey -> Icons.Filled.Face
    ExploreNavKey -> Icons.Filled.Explore
    ModelsNavKey -> Icons.Filled.Widgets
    else -> Icons.Filled.Face
  }
}

fun getLabelForKey(key: NavKey): String {
  return when (key) {
    ChatNavKey -> "对话"
    AgentNavKey -> "智能体"
    ExploreNavKey -> "探索"
    ModelsNavKey -> "模型管理"
    else -> ""
  }
}
