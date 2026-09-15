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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatusType
import com.sofar.core.ai.edge.design.theme.elevations
import com.sofar.core.ai.edge.design.theme.iconSizes
import com.sofar.core.ai.edge.design.theme.spacing
import com.sofar.core.ai.edge.design.theme.widgetSizes
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

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = MaterialTheme.spacing.lg),
    contentPadding = PaddingValues(bottom = MaterialTheme.spacing.lg)
  ) {
    item {
      Text(
        text = stringResource(id = R.string.feature_models_storage_hub_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(
          top = MaterialTheme.spacing.lg,
          bottom = MaterialTheme.spacing.sm
        )
      )
    }

    item {
      StorageCard(uiState)
    }

    item {
      Column {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
        Text(
          text = stringResource(id = R.string.feature_models_repository_title),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(bottom = MaterialTheme.spacing.sm)
        )
      }
    }

    itemsIndexed(
      items = uiState.models,
      key = { _, model -> model.name }
    ) { index, model ->
      if (index > 0) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
      }
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

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = MaterialTheme.elevations.card)
  ) {
    Row(
      modifier = Modifier
        .padding(MaterialTheme.spacing.lg)
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
          strokeWidth = MaterialTheme.elevations.trackThickness,
          strokeCap = StrokeCap.Round,
          trackColor = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
          text = "${(progress * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.width(MaterialTheme.spacing.xl))

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
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
          onClick = { /* TODO: Optimize */ },
          modifier = Modifier
            .padding(top = MaterialTheme.spacing.md)
            .height(MaterialTheme.widgetSizes.buttonHeightSmall),
          contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg, vertical = 0.dp)
        ) {
          Text(
            text = stringResource(id = R.string.feature_models_btn_optimize),
            style = MaterialTheme.typography.labelMedium
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
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = MaterialTheme.elevations.card)
  ) {
    Column(modifier = Modifier.padding(MaterialTheme.spacing.lg)) {
      ModelHeader(
        name = model.name,
        sizeInBytes = model.sizeInBytes,
        onDelete = onDelete
      )

      ModelMarkdown(info = model.info)

      ModelActionBar(
        model = model,
        status = status,
        onDownload = onDownload,
        onTry = onTry
      )
    }
  }
}

@Composable
private fun ModelHeader(
  name: String,
  sizeInBytes: Long,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          painter = painterResource(id = coreIconR.drawable.core_ic_circle_download),
          contentDescription = null,
          modifier = Modifier.size(MaterialTheme.iconSizes.small),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.xs))
        Text(
          text = Formatter.formatFileSize(LocalContext.current, sizeInBytes),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    IconButton(
      onClick = onDelete,
      modifier = Modifier
        .align(Alignment.Top)
        .offset(x = 12.dp, y = (-12).dp)
    ) {
      Icon(
        painter = painterResource(id = coreIconR.drawable.core_ic_delete),
        contentDescription = "Delete",
        tint = MaterialTheme.colorScheme.outline,
        modifier = Modifier.size(MaterialTheme.iconSizes.medium)
      )
    }
  }
}

@Composable
private fun ModelMarkdown(
  info: String
) {
  // 性能优化：第三方 Markdown 组件因参数不稳定（Unstable）会导致列表滚动时频繁被穿透重组。
  // 解决方案：使用 `movableContentOf` 配合 `remember(info)` 将其固化在缓存插槽中。
  // 只要文本不变，闭包地址就不变，从而强行触发 `Skip（跳过重组）`，确保列表滑动达到极致丝滑。
  val content = remember(info) {
    movableContentOf {
      Markdown(
        content = info,
        modifier = Modifier.padding(vertical = MaterialTheme.spacing.md),
        colors = markdownColor(
          text = MaterialTheme.colorScheme.outline,
          codeBackground = MaterialTheme.colorScheme.surfaceVariant
        ),
        typography = markdownTypography(
          text = MaterialTheme.typography.bodySmall,
          paragraph = MaterialTheme.typography.bodySmall,
          list = MaterialTheme.typography.bodySmall,
          textLink = TextLinkStyles(
            style = SpanStyle(
              color = MaterialTheme.colorScheme.primary,
              textDecoration = TextDecoration.Underline,
            )
          )
        ),
      )
    }
  }
  content()
}

@Composable
private fun ModelActionBar(
  model: Model,
  status: ModelDownloadStatus?,
  onDownload: () -> Unit,
  onTry: () -> Unit,
  modifier: Modifier = Modifier
) {
  val statusType = status?.statusType ?: ModelDownloadStatusType.NOT_DOWNLOADED

  if (statusType == ModelDownloadStatusType.SUCCEEDED) {
    Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
      if (model.showBenchmarkButton) {
        FilledTonalButton(
          onClick = { /* Benchmark */ },
          modifier = Modifier.weight(1f),
          contentPadding = PaddingValues(0.dp)
        ) {
          Text(
            text = stringResource(id = R.string.feature_models_benchmark),
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
      if (model.showRunAgainButton) {
        Button(
          onClick = onTry,
          modifier = Modifier.weight(1.2f),
          contentPadding = PaddingValues(0.dp)
        ) {
          Text(
            text = stringResource(id = R.string.feature_models_try_it),
            style = MaterialTheme.typography.labelMedium
          )
        }
      }
    }
  } else {
    val buttonText = getDownloadButtonText(status)
    val isEnabled = statusType != ModelDownloadStatusType.IN_PROGRESS &&
        statusType != ModelDownloadStatusType.UNZIPPING

    Button(
      onClick = onDownload,
      modifier = modifier.fillMaxWidth(),
      enabled = isEnabled
    ) {
      Icon(
        painter = painterResource(id = coreIconR.drawable.core_ic_download),
        contentDescription = null,
        modifier = Modifier.size(MaterialTheme.iconSizes.small)
      )
      Spacer(modifier = Modifier.width(MaterialTheme.spacing.sm))
      Text(
        text = buttonText,
        style = MaterialTheme.typography.labelMedium
      )
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
