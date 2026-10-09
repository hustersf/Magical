package com.sofar.feature.ai.edge.chat.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofar.core.ai.edge.data.entity.chat.ChatPriority
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.database.entity.SessionEntity
import com.sofar.core.ai.edge.design.theme.iconSizes
import com.sofar.core.ai.edge.design.theme.spacing
import com.sofar.core.ai.edge.design.theme.widgetSizes
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.chat.api.navigation.ChatDetailNavKey
import com.sofar.feature.ai.edge.chat.logic.home.ChatHomeViewModel
import java.util.UUID
import com.sofar.core.res.icon.R as coreIconR
import com.sofar.feature.ai.edge.chat.api.R as chatR

/**
 * 聊天首页私有维度定义（特定业务约束）
 */
private object ChatHomeDimens {
  val sessionItemHeight = 80.dp
  val avatarContainerSize = 56.dp
}

@Composable
fun ChatHomeScreen(
  navigator: Navigator,
  viewModel: ChatHomeViewModel = hiltViewModel(),
) {
  val sessions by viewModel.sessionListState.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    ChatHeader(onCreateClick = {
      val newSessionId = UUID.randomUUID().toString()
      navigator.navigate(ChatDetailNavKey(sessionId = newSessionId))
    })

    if (sessions.isEmpty()) {
      ChatEmptyState()
    } else {
      LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = sessions, key = { it.id }) { session ->
          SessionItemRow(
            session = session,
            viewModel = viewModel,
            onClick = {
              navigator.navigate(
                ChatDetailNavKey(
                  sessionId = session.id,
                  agentId = session.agentId
                )
              )
            }
          )
        }
      }
    }
  }
}

@Composable
private fun ChatHeader(onCreateClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = MaterialTheme.spacing.lg)
      .padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Spacer(modifier = Modifier.size(MaterialTheme.widgetSizes.touchTargetIcon))
    Text(
      text = stringResource(id = chatR.string.feature_chat_home_title),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center
    )
    IconButton(
      onClick = onCreateClick,
      modifier = Modifier.size(MaterialTheme.widgetSizes.touchTargetIcon)
    ) {
      Icon(
        painter = painterResource(id = coreIconR.drawable.core_ic_edit_note),
        contentDescription = "New Chat",
        modifier = Modifier.size(MaterialTheme.iconSizes.medium)
      )
    }
  }
}

@Composable
private fun ChatEmptyState() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(MaterialTheme.spacing.lg),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(id = chatR.string.feature_chat_title_default),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun SessionItemRow(
  session: SessionEntity,
  viewModel: ChatHomeViewModel,
  onClick: () -> Unit
) {
  val isPinned = session.priority > ChatPriority.NORMAL
  var showMenu by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(ChatHomeDimens.sessionItemHeight)
      .background(if (isPinned) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface)
      .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
      .padding(horizontal = MaterialTheme.spacing.lg)
  ) {
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
      val avatarText: String
      var previewText =
        stringResource(id = chatR.string.feature_chat_home_session_type_default_preview)
      var tagText: String? = null
      val avatarBgColor: Color

      when (session.type) {
        ChatSessionType.VISION -> {
          avatarText = "🖼️"
          previewText =
            stringResource(id = chatR.string.feature_chat_home_session_type_vision_preview)
          avatarBgColor = MaterialTheme.colorScheme.primaryContainer
        }

        ChatSessionType.MEETING -> {
          avatarText = "🎙️"
          previewText =
            stringResource(id = chatR.string.feature_chat_home_session_type_meeting_preview)
          avatarBgColor = MaterialTheme.colorScheme.secondaryContainer
        }

        ChatSessionType.TEXT -> {
          if (session.agentId.isNullOrEmpty()) {
            avatarText = "💬"
            previewText =
              stringResource(id = chatR.string.feature_chat_home_session_type_text_empty_preview)
            avatarBgColor = MaterialTheme.colorScheme.tertiaryContainer
          } else {
            val agent = viewModel.agentCache.get(session.agentId)
            avatarText = agent?.avatar ?: "🤖"
            previewText =
              stringResource(id = chatR.string.feature_chat_home_session_type_agent_preview)
            tagText = agent?.name
            avatarBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
          }
        }

        else -> {
          avatarText = "✉️"
          previewText =
            stringResource(id = chatR.string.feature_chat_home_session_type_default_preview)
          avatarBgColor = MaterialTheme.colorScheme.surfaceVariant
        }
      }

      session.lastMessage?.let { previewText = it }

      Box(
        modifier = Modifier
          .size(ChatHomeDimens.avatarContainerSize)
          .background(color = avatarBgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(text = avatarText, style = MaterialTheme.typography.headlineMedium)
      }

      Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = session.title.ifEmpty { stringResource(id = chatR.string.feature_chat_title_default) },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          tagText?.let {
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.xs))
            Box(
              modifier = Modifier
                .background(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.xs)
            ) {
              Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs / 2))
        Text(
          text = previewText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
      DropdownMenuItem(
        text = { Text(text = stringResource(id = chatR.string.feature_chat_action_clear_history)) },
        onClick = { viewModel.clearChatHistory(session); showMenu = false })
      if (!isPinned) DropdownMenuItem(text = {
        Text(
          text = stringResource(id = chatR.string.feature_chat_action_delete),
          color = MaterialTheme.colorScheme.error
        )
      }, onClick = { viewModel.deleteSession(session); showMenu = false })
    }
  }
}
