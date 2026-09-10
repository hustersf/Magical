package com.sofar.feature.ai.edge.models.impl

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatusType
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.feature.ai.edge.chat.api.navigation.ChatNavKey
import com.sofar.feature.ai.edge.models.api.R
import com.sofar.feature.ai.edge.models.logic.ModelManagerUiState
import com.sofar.feature.ai.edge.models.logic.ModelsManagerViewModel
import com.sofar.core.res.icon.R as coreIconR

@Composable
internal fun ModelsScreen(
  viewModel: ModelsManagerViewModel = hiltViewModel(),
  navigator: Navigator
) {
  val uiState by viewModel.uiState.collectAsState()
  var showDeleteDialog by remember { mutableStateOf<Model?>(null) }

  LaunchedEffect(Unit) {
    viewModel.fetchConfig()
  }

  Scaffold { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
      item {
        Text(
          text = stringResource(id = R.string.feature_models_storage_hub_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      item {
        StorageCard(uiState)
      }

      item {
        Text(
          text = stringResource(id = R.string.feature_models_repository_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      items(uiState.models) { model ->
        val status = uiState.modelDownloadStatus[model.name]
        ModelItem(
          model = model,
          status = status,
          onDownload = { viewModel.downloadModel(model) },
          onDelete = { showDeleteDialog = model },
          onTry = {
            viewModel.selectActiveModel(model)
            navigator.navigate(ChatNavKey)
          }
        )
      }
    }
  }

  showDeleteDialog?.let { model ->
    DeleteConfirmDialog(
      modelName = model.name,
      onConfirm = {
        viewModel.deleteModel(model)
        showDeleteDialog = null
      },
      onDismiss = { showDeleteDialog = null }
    )
  }
}

@Composable
private fun StorageCard(state: ModelManagerUiState) {
  val context = LocalContext.current
  val snapshot = state.storageSnapshot

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(contentAlignment = Alignment.Center) {
        val progress = if (snapshot != null) {
          val total = snapshot.modelsSize + snapshot.availableSize
          if (total > 0) snapshot.modelsSize.toFloat() / total else 0f
        } else 0f

        CircularProgressIndicator(
          progress = { progress },
          modifier = Modifier.size(80.dp),
          strokeWidth = 5.dp,
          strokeCap = StrokeCap.Round,
          trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
          text = "${(progress * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(20.dp))

      Column {
        val usedStr =
          snapshot?.let { Formatter.formatFileSize(context, it.modelsSize) } ?: "0B"
        val availStr =
          snapshot?.let { Formatter.formatFileSize(context, it.availableSize) } ?: "0B"

        Text(
          text = stringResource(
            id = R.string.feature_models_storage_used_format,
            usedStr,
            availStr
          ),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
          onClick = { /* TODO: Optimize */ },
          modifier = Modifier
            .padding(top = 12.dp)
            .height(36.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
          Text(
            text = stringResource(id = R.string.feature_models_btn_optimize),
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }
}

@Composable
private fun ModelItem(
  model: Model,
  status: ModelDownloadStatus?,
  onDownload: () -> Unit,
  onDelete: () -> Unit,
  onTry: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = model.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete) {
          Icon(
            painter = painterResource(id = coreIconR.drawable.core_ic_delete),
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Icon(
          painter = painterResource(id = coreIconR.drawable.core_ic_circle_download),
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = Formatter.formatFileSize(LocalContext.current, model.sizeInBytes),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Markdown(
        content = model.info,
        modifier = Modifier.padding(vertical = 12.dp)
      )

      val statusType = status?.statusType ?: ModelDownloadStatusType.NOT_DOWNLOADED

      if (statusType == ModelDownloadStatusType.SUCCEEDED) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (model.showBenchmarkButton) {
            OutlinedButton(
              onClick = { /* Benchmark */ },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(text = stringResource(id = R.string.feature_models_benchmark))
            }
          }
          if (model.showRunAgainButton) {
            Button(
              onClick = onTry,
              modifier = Modifier.weight(1.2f),
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(text = stringResource(id = R.string.feature_models_try_it))
            }
          }
        }
      } else {
        val buttonText = getDownloadButtonText(status)
        val isEnabled = statusType != ModelDownloadStatusType.IN_PROGRESS &&
            statusType != ModelDownloadStatusType.UNZIPPING

        Button(
          onClick = onDownload,
          modifier = Modifier.fillMaxWidth(),
          enabled = isEnabled
        ) {
          Icon(
            painter = painterResource(id = coreIconR.drawable.core_ic_download),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = buttonText)
        }
      }
    }
  }
}

@Composable
private fun getDownloadButtonText(status: ModelDownloadStatus?): String {
  val context = LocalContext.current
  if (status == null) return stringResource(id = R.string.feature_models_download)

  val progressInt = if (status.totalBytes > 0L) {
    ((status.receivedBytes * 100) / status.totalBytes).toInt()
  } else {
    0
  }

  return when (status.statusType) {
    ModelDownloadStatusType.NOT_DOWNLOADED -> stringResource(id = R.string.feature_models_download)
    ModelDownloadStatusType.PARTIALLY_DOWNLOADED -> stringResource(
      id = R.string.feature_models_download_partial,
      progressInt
    )

    ModelDownloadStatusType.IN_PROGRESS -> {
      val currentStr = Formatter.formatFileSize(context, status.receivedBytes)
      val totalStr = Formatter.formatFileSize(context, status.totalBytes)
      val remainingSeconds = if (status.bytesPerSecond > 0L) {
        (status.totalBytes - status.receivedBytes) / status.bytesPerSecond
      } else {
        0L
      }
      val minutes = (remainingSeconds / 60).coerceAtLeast(1)
      val remainingTimeStr =
        stringResource(id = R.string.feature_models_time_minutes, minutes)
      stringResource(
        id = R.string.feature_models_download_progress,
        progressInt,
        currentStr,
        totalStr,
        remainingTimeStr
      )
    }

    ModelDownloadStatusType.UNZIPPING -> stringResource(id = R.string.feature_models_download_unzipping)
    ModelDownloadStatusType.FAILED -> stringResource(id = R.string.feature_models_download_retry)
    else -> stringResource(id = R.string.feature_models_download)
  }
}

@Composable
private fun DeleteConfirmDialog(
  modelName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = stringResource(id = R.string.feature_models_delete_dialog_title)) },
    text = {
      Text(
        text = stringResource(
          id = R.string.feature_models_delete_dialog_message,
          modelName
        )
      )
    },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = stringResource(id = R.string.feature_models_delete_dialog_confirm),
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = R.string.feature_models_delete_dialog_cancel))
      }
    }
  )
}
