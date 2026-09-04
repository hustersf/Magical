package com.sofar.ai.edge.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.sofar.core.res.icon.R as coreIconR
import com.sofar.feature.ai.edge.agent.api.R as agentR
import com.sofar.feature.ai.edge.chat.api.R as chatR
import com.sofar.feature.ai.edge.explore.api.R as exploreR
import com.sofar.feature.ai.edge.models.api.R as modelsR

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
            icon = {
              Icon(
                painter = painterResource(id = getIconResForKey(key)),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            },
            label = { Text(text = stringResource(id = getLabelResForKey(key))) },
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

fun getIconResForKey(key: NavKey): Int {
  return when (key) {
    ChatNavKey -> coreIconR.drawable.core_ic_chat
    AgentNavKey -> coreIconR.drawable.core_ic_agent
    ExploreNavKey -> coreIconR.drawable.core_ic_explore
    ModelsNavKey -> coreIconR.drawable.core_ic_setting
    else -> coreIconR.drawable.core_ic_agent
  }
}

fun getLabelResForKey(key: NavKey): Int {
  return when (key) {
    ChatNavKey -> chatR.string.feature_chat_tab_name
    AgentNavKey -> agentR.string.feature_agent_tab_name
    ExploreNavKey -> exploreR.string.feature_explore_tab_name
    ModelsNavKey -> modelsR.string.feature_models_tab_name
    else -> agentR.string.feature_agent_tab_name
  }
}
