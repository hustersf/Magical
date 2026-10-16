package com.sofar.feature.ai.edge.agent.impl

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofar.core.ai.edge.data.entity.agent.AgentSourceType
import com.sofar.core.ai.edge.database.entity.AgentEntity
import com.sofar.core.ai.edge.design.theme.elevations
import com.sofar.core.ai.edge.design.theme.iconSizes
import com.sofar.core.ai.edge.design.theme.spacing
import com.sofar.core.ai.edge.design.theme.widgetSizes
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.agent.logic.AgentHomeViewModel
import com.sofar.feature.ai.edge.chat.api.navigation.ChatDetailNavKey
import com.sofar.core.res.icon.R as coreIconR
import com.sofar.feature.ai.edge.agent.api.R as agentR

@Composable
internal fun AgentScreen(
  viewModel: AgentHomeViewModel = hiltViewModel(),
  navigator: Navigator,
) {
  val context = LocalContext.current
  val agentList by viewModel.agentListState.collectAsState()

  var showEditorDialogByAgent by remember { mutableStateOf<AgentEntity?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var showDeleteConfirmDialogByAgent by remember { mutableStateOf<AgentEntity?>(null) }

  LaunchedEffect(Unit) {
    viewModel.messageFlow.collect { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = MaterialTheme.spacing.lg)
  ) {
    AgentHeader(onCreateClick = { showCreateDialog = true })

    if (agentList.isEmpty()) {
      AgentEmptyState()
    } else {
      AgentListContent(
        agentList = agentList,
        onAgentClick = { agent -> navigator.navigate(ChatDetailNavKey.newSession(agent.id)) },
        onEditClick = { showEditorDialogByAgent = it },
        onDeleteClick = { showDeleteConfirmDialogByAgent = it }
      )
    }
  }

  if (showCreateDialog) {
    AgentEditorDialog(
      agent = null,
      onDismiss = { showCreateDialog = false },
      onConfirm = { name, avatar, prompt ->
        viewModel.createAgent(name, avatar, prompt)
      }
    )
  }

  showEditorDialogByAgent?.let { agent ->
    AgentEditorDialog(
      agent = agent,
      onDismiss = { showEditorDialogByAgent = null },
      onConfirm = { name, avatar, prompt ->
        viewModel.updateAgent(agent.id, name, avatar, prompt)
      }
    )
  }

  showDeleteConfirmDialogByAgent?.let { agent ->
    DeleteConfirmDialog(
      agentName = agent.name,
      onDismiss = { showDeleteConfirmDialogByAgent = null },
      onConfirm = {
        viewModel.deleteAgent(agent)
      }
    )
  }
}

@Composable
private fun AgentHeader(onCreateClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = MaterialTheme.spacing.lg, bottom = MaterialTheme.spacing.sm),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Spacer(modifier = Modifier.size(MaterialTheme.widgetSizes.touchTargetIcon))
    Text(
      text = stringResource(id = agentR.string.feature_agent_home_title),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.Center
    )
    IconButton(
      onClick = onCreateClick,
      modifier = Modifier.size(MaterialTheme.widgetSizes.touchTargetIcon)
    ) {
      Icon(
        painter = painterResource(id = coreIconR.drawable.core_ic_circle_more),
        contentDescription = stringResource(id = agentR.string.feature_agent_action_create),
        modifier = Modifier.size(MaterialTheme.iconSizes.medium)
      )
    }
  }
}

@Composable
private fun AgentEmptyState() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(MaterialTheme.spacing.lg),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(id = agentR.string.feature_agent_empty_state),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun AgentListContent(
  agentList: List<AgentEntity>,
  onAgentClick: (AgentEntity) -> Unit,
  onEditClick: (AgentEntity) -> Unit,
  onDeleteClick: (AgentEntity) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(bottom = MaterialTheme.spacing.lg)
  ) {
    items(
      items = agentList,
      key = { it.id }
    ) { agent ->
      AgentItemCard(
        agent = agent,
        onClick = { onAgentClick(agent) },
        onEditClick = { onEditClick(agent) },
        onDeleteClick = { onDeleteClick(agent) }
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AgentItemCard(
  agent: AgentEntity,
  onClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .combinedClickable(
        onClick = onClick,
        onLongClick = {
          if (agent.sourceType != AgentSourceType.SYSTEM) {
            showMenu = true
          }
        }
      ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = MaterialTheme.elevations.card)
  ) {
    Box {
      Row(
        modifier = Modifier
          .padding(MaterialTheme.spacing.lg)
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .background(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = agent.avatar.ifEmpty { "🤖" },
            fontSize = 24.sp
          )
        }

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = agent.name,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            if (agent.sourceType == AgentSourceType.SYSTEM) {
              Spacer(modifier = Modifier.width(MaterialTheme.spacing.xs))
              Box(
                modifier = Modifier
                  .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                  )
                  .padding(horizontal = MaterialTheme.spacing.xs, vertical = 2.dp)
              ) {
                Text(
                  text = stringResource(id = agentR.string.feature_agent_tag_official),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

          Text(
            text = agent.systemPrompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
      ) {
        DropdownMenuItem(
          text = { Text(text = stringResource(id = agentR.string.feature_agent_action_edit)) },
          onClick = {
            showMenu = false
            onEditClick()
          }
        )
        DropdownMenuItem(
          text = { Text(text = stringResource(id = agentR.string.feature_agent_action_delete)) },
          onClick = {
            showMenu = false
            onDeleteClick()
          }
        )
      }
    }
  }
}

@Composable
private fun AgentEditorDialog(
  agent: AgentEntity?,
  onDismiss: () -> Unit,
  onConfirm: (String, String, String) -> Unit
) {
  var avatar by remember { mutableStateOf(agent?.avatar ?: "🤖") }
  var name by remember { mutableStateOf(agent?.name.orEmpty()) }
  var prompt by remember { mutableStateOf(agent?.systemPrompt ?: "") }

  val defaultPrompt = stringResource(id = agentR.string.feature_agent_default_prompt_content)
  LaunchedEffect(agent) {
    if (agent == null && prompt.isEmpty()) {
      prompt = defaultPrompt
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(
          id = if (agent == null) agentR.string.feature_agent_create_title else agentR.string.feature_agent_edit_title
        )
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = avatar,
          onValueChange = { avatar = it },
          label = { Text(text = stringResource(id = agentR.string.feature_agent_field_avatar)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(text = stringResource(id = agentR.string.feature_agent_field_name)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        OutlinedTextField(
          value = prompt,
          onValueChange = { prompt = it },
          label = { Text(text = stringResource(id = agentR.string.feature_agent_field_prompt)) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 4
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(name, avatar, prompt)
          onDismiss()
        }
      ) {
        Text(
          text = stringResource(
            id = if (agent == null) agentR.string.feature_agent_action_create else agentR.string.feature_agent_action_save
          )
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = agentR.string.feature_agent_action_cancel))
      }
    }
  )
}

@Composable
private fun DeleteConfirmDialog(
  agentName: String,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(id = agentR.string.feature_agent_delete_title)) },
    text = {
      Text(text = stringResource(id = agentR.string.feature_agent_delete_message, agentName))
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm()
          onDismiss()
        }
      ) {
        Text(
          text = stringResource(id = agentR.string.feature_agent_action_delete),
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = agentR.string.feature_agent_action_cancel))
      }
    }
  )
}
