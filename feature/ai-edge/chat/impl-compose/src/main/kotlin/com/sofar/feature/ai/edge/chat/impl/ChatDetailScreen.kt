package com.sofar.feature.ai.edge.chat.impl

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageRole
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ai.edge.design.theme.corners
import com.sofar.core.ai.edge.design.theme.elevations
import com.sofar.core.ai.edge.design.theme.iconSizes
import com.sofar.core.ai.edge.design.theme.spacing
import com.sofar.core.ai.edge.design.theme.widgetSizes
import com.sofar.core.ai.edge.navigation.Navigator
import com.sofar.core.media.GetMediaContract
import com.sofar.core.media.MediaAction
import com.sofar.core.speech.SpeechRecognitionClient
import com.sofar.core.speech.SpeechRecognitionEvent
import com.sofar.core.speech.SpeechRecognitionRequest
import com.sofar.feature.ai.edge.chat.logic.detail.ChatDetailEffect
import com.sofar.feature.ai.edge.chat.logic.detail.ChatDetailUiState
import com.sofar.feature.ai.edge.chat.logic.detail.ChatDetailViewModel
import com.sofar.feature.ai.edge.chat.logic.detail.image.SelectedImageState
import com.sofar.feature.ai.edge.chat.logic.detail.voice.VoiceInputUiState
import com.sofar.image.fetchImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import com.sofar.core.res.icon.R as coreIconR
import com.sofar.feature.ai.edge.chat.api.R as chatR

/**
 * 聊天详情页私有维度定义（特定业务约束）
 */
private object ChatDetailDimens {
  val deleteBadgeSize = 18.dp
  val messageImageMaxWidth = 165.dp
  val messageImageMaxHeight = 265.dp
  val multiImageItemSize = 80.dp
  val messageBubbleMaxWidth = 300.dp
  val voiceOverlayCircleSize = 100.dp
  const val SLIDE_UP_CANCEL_THRESHOLD_PX = 72f
}

/**
 * UI 层专用稳态包装类
 */
@Immutable
private data class MessageUiItem(
  val message: MessageEntity,
  val isStreaming: Boolean
)

/**
 * 聊天详情主屏 - 负责多模态交互协调与生命周期分发
 */
@Composable
fun ChatDetailScreen(
  sessionId: String,
  agentId: String? = null,
  navigator: Navigator,
  viewModel: ChatDetailViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  // 基础设施生命周期管理
  val mediaLauncher = rememberLauncherForActivityResult(GetMediaContract()) { paths ->
    if (paths.isNotEmpty()) {
      viewModel.handleSelectedImages(paths)
    }
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (!isGranted) Toast.makeText(
        context,
        chatR.string.feature_chat_voice_permission_denied,
        Toast.LENGTH_SHORT
      ).show()
    }

  val speechClient = remember {
    SpeechRecognitionClient(
      context,
      scope,
      SpeechRecognitionRequest("zh-CN", true)
    ).apply { prepare() }
  }

  var alertDialogMessage by remember { mutableStateOf<String?>(null) }
  var previewImagePath by remember { mutableStateOf<String?>(null) }

  // 业务状态初始化
  LaunchedEffect(sessionId, agentId) {
    viewModel.init(sessionId, agentId)
    viewModel.effectFlow.collectLatest { effect ->
      when (effect) {
        is ChatDetailEffect.ShowToast -> Toast.makeText(
          context,
          effect.message,
          Toast.LENGTH_SHORT
        ).show()

        is ChatDetailEffect.ShowAlert -> alertDialogMessage = effect.message
      }
    }
  }

  // 监听语音识别回调
  LaunchedEffect(speechClient) {
    withContext(Dispatchers.Main.immediate) {
      speechClient.events.collect { event ->
        handleSpeechEvent(event, viewModel, sessionId, agentId, context)
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose { speechClient.release() }
  }

  alertDialogMessage?.let { message ->
    AlertDialog(
      onDismissRequest = { alertDialogMessage = null },
      title = { Text(text = stringResource(chatR.string.feature_chat_detail_error_title)) },
      text = { Text(text = message) },
      confirmButton = {
        TextButton(onClick = { alertDialogMessage = null }) {
          Text(text = stringResource(chatR.string.feature_chat_detail_ok))
        }
      }
    )
  }

  previewImagePath?.let { path ->
    ImagePreviewDialog(
      imagePath = path,
      onDismiss = { previewImagePath = null }
    )
  }

  Scaffold(
    topBar = { ChatDetailTopBar(uiState.sessionTitle, onBack = { navigator.goBack() }) }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // 核心消息展示区
        ChatMessageList(
          modifier = Modifier.weight(1f),
          messages = uiState.messages,
          listState = listState,
          isAiResponding = uiState.isAiResponding,
          onImageClick = { previewImagePath = it }
        )
        // 底部多模态输入面板
        InputPanel(
          uiState = uiState,
          viewModel = viewModel,
          mediaLauncher = mediaLauncher,
          permissionLauncher = permissionLauncher,
          speechClient = speechClient,
          sessionId = sessionId,
          agentId = agentId
        )
      }
      // 语音交互状态遮罩
      VoiceOverlayPanel(voiceState = uiState.voiceState)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailTopBar(title: String, onBack: () -> Unit) {
  CenterAlignedTopAppBar(
    title = {
      Text(
        text = title.ifEmpty { stringResource(chatR.string.feature_chat_home_title) },
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    navigationIcon = {
      IconButton(
        onClick = onBack,
      ) {
        Icon(
          painter = painterResource(coreIconR.drawable.core_ic_back),
          contentDescription = "Back",
          modifier = Modifier.size(MaterialTheme.iconSizes.medium)
        )
      }
    }
  )
}

/**
 * 消息列表容器
 * 采用逆向布局 (Reverse Layout)，坐标原点锚定在屏幕底端，使新消息在视觉上天然吸附于输入框上方。
 */
@Composable
private fun ChatMessageList(
  modifier: Modifier = Modifier,
  messages: List<MessageEntity>,
  listState: LazyListState,
  isAiResponding: Boolean,
  onImageClick: (String) -> Unit
) {
  val displayMessages = remember(messages) {
    messages.asReversed()
  }

  LazyColumn(
    state = listState,
    reverseLayout = true,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = MaterialTheme.spacing.lg),
    contentPadding = PaddingValues(vertical = MaterialTheme.spacing.lg),
    verticalArrangement = Arrangement.Top
  ) {
    itemsIndexed(items = displayMessages, key = { _, it -> it.id }) { index, message ->
      val isStreaming = index == 0 && isAiResponding && message.role != ChatMessageRole.USER
      val uiItem = remember(message, isStreaming) {
        MessageUiItem(message, isStreaming)
      }
      MessageBubbleItem(uiItem = uiItem, onImageClick = onImageClick)
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
    }
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.scrollToItem(0)
    }
  }
}

@Composable
private fun InputPanel(
  uiState: ChatDetailUiState,
  viewModel: ChatDetailViewModel,
  mediaLauncher: ActivityResultLauncher<MediaAction>,
  permissionLauncher: ActivityResultLauncher<String>,
  speechClient: SpeechRecognitionClient,
  sessionId: String,
  agentId: String?
) {
  Surface(
    modifier = Modifier
      .padding(MaterialTheme.spacing.lg)
      .fillMaxWidth(),
    shape = RoundedCornerShape(MaterialTheme.corners.lg),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
    border = BorderStroke(
      MaterialTheme.elevations.stroke,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
  ) {
    Column(modifier = Modifier.padding(MaterialTheme.spacing.sm)) {
      if (uiState.selectedImages.isNotEmpty()) {
        SelectedImagesRow(
          images = uiState.selectedImages,
          onAddClick = { mediaLauncher.launch(MediaAction.PICK_IMAGE) },
          onDeleteClick = { imageState ->
            viewModel.removeSelectedImage(imageState)
          }
        )
      }
      BottomActionPanel(
        uiState = uiState,
        viewModel = viewModel,
        mediaLauncher = mediaLauncher,
        permissionLauncher = permissionLauncher,
        speechRecognitionClient = speechClient,
        sessionId = sessionId,
        agentId = agentId
      )
    }
  }
}

@Composable
private fun SelectedImagesRow(
  images: List<SelectedImageState>,
  onAddClick: () -> Unit,
  onDeleteClick: (SelectedImageState) -> Unit
) {
  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = MaterialTheme.spacing.sm),
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    verticalAlignment = Alignment.CenterVertically
  ) {
    itemsIndexed(
      items = images,
      key = { index, imageState -> "${imageState.path}_$index" }) { _, imageState ->
      Box(modifier = Modifier.size(MaterialTheme.iconSizes.xxl)) {
        LocalImage(
          path = imageState.path.orEmpty(),
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(MaterialTheme.corners.xs))
        )
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(ChatDetailDimens.deleteBadgeSize)
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .clickable { onDeleteClick(imageState) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "×",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
          )
        }
      }
    }
    item {
      Box(
        modifier = Modifier
          .size(MaterialTheme.iconSizes.xxl)
          .background(
            MaterialTheme.colorScheme.surface,
            RoundedCornerShape(MaterialTheme.corners.xs)
          )
          .clickable(onClick = onAddClick),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(coreIconR.drawable.core_ic_add),
          contentDescription = "Add Image",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(MaterialTheme.iconSizes.medium)
        )
      }
    }
  }
}

/**
 * 消息气泡原子组件
 */
@Composable
private fun MessageBubbleItem(
  uiItem: MessageUiItem,
  onImageClick: (String) -> Unit
) {
  val message = uiItem.message
  val isStreaming = uiItem.isStreaming
  val isUser = message.role == ChatMessageRole.USER

  val paths = message.filePath ?: emptyList()
  val hasText = !message.textContent.isNullOrEmpty()

  val showTextBubble =
    (isUser && hasText) || (!isUser && (hasText || isStreaming || message.textContent.isNullOrEmpty()))

  val displayContent = message.textContent.takeIf { !it.isNullOrEmpty() }
    ?: if (!isUser) stringResource(chatR.string.feature_chat_ai_thinking) else ""

  val hasMarkdown = remember(message.textContent) {
    message.textContent?.contains(Regex("[#*`>\\[\\]|]")) ?: false
  }

  val useTextOnly = isUser || isStreaming || message.textContent.isNullOrEmpty() || !hasMarkdown

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    // 图片区域（单图按比例，多图横向列表）
    if (paths.isNotEmpty()) {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
      ) {
        if (paths.size == 1) {
          LocalImage(
            path = paths.first(),
            modifier = Modifier
              .sizeIn(
                maxWidth = ChatDetailDimens.messageImageMaxWidth,
                maxHeight = ChatDetailDimens.messageImageMaxHeight
              )
              .clip(RoundedCornerShape(MaterialTheme.corners.md))
              .clickable { onImageClick(paths.first()) },
            adjustViewBounds = true
          )
        } else {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
          ) {
            itemsIndexed(items = paths, key = { index, path -> "${path}_$index" }) { _, path ->
              LocalImage(
                path = path,
                modifier = Modifier
                  .size(ChatDetailDimens.multiImageItemSize)
                  .clip(RoundedCornerShape(MaterialTheme.corners.md))
                  .clickable { onImageClick(path) }
              )
            }
          }
        }
      }
    }

    // 图片与文本消息之间的额外小间距
    if (paths.isNotEmpty() && showTextBubble) {
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
    }

    // 文本气泡区域（如果为纯图用户消息，隐藏气泡）
    if (showTextBubble) {
      Surface(
        shape = RoundedCornerShape(
          topStart = MaterialTheme.corners.lg,
          topEnd = MaterialTheme.corners.lg,
          bottomStart = if (isUser) MaterialTheme.corners.lg else MaterialTheme.corners.xs,
          bottomEnd = if (isUser) MaterialTheme.corners.xs else MaterialTheme.corners.lg
        ),
        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(
          alpha = 0.7f
        ),
        modifier = Modifier.widthIn(max = ChatDetailDimens.messageBubbleMaxWidth)
      ) {
        Box(
          modifier = Modifier.padding(
            horizontal = MaterialTheme.spacing.lg,
            vertical = MaterialTheme.spacing.sm
          )
        ) {
          if (useTextOnly) {
            Text(
              text = displayContent,
              style = MaterialTheme.typography.bodyLarge,
              color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Markdown(
              content = displayContent,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

/**
 * Compose 全屏大图预览对话框（使用现代 Compose Edge-to-Edge 沉浸式 API）
 */
@Composable
private fun ImagePreviewDialog(
  imagePath: String,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false,
      decorFitsSystemWindows = false // 现代 Edge-to-Edge API，自动沉浸到系统栏（Status Bar 和 Navigation Bar）下方
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .clickable(onClick = onDismiss)
    ) {
      // 1. 关闭按钮（使用 statusBarsPadding 自动响应顶部状态栏 Safe Area）
      IconButton(
        onClick = onDismiss,
        modifier = Modifier
          .statusBarsPadding()
          .padding(start = MaterialTheme.spacing.xs, top = MaterialTheme.spacing.xs)
      ) {
        Icon(
          painter = painterResource(coreIconR.drawable.core_ic_cancel),
          contentDescription = "Close",
          tint = Color.White,
          modifier = Modifier.size(MaterialTheme.iconSizes.medium)
        )
      }

      // 2. 大图展示（使用 navigationBarsPadding 自动响应底部导航栏 Safe Area）
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .navigationBarsPadding()
          .padding(
            top = MaterialTheme.spacing.lg,
            bottom = MaterialTheme.spacing.xl,
            start = MaterialTheme.spacing.md,
            end = MaterialTheme.spacing.md
          ),
        contentAlignment = Alignment.Center
      ) {
        LocalImage(
          path = imagePath,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Fit
        )
      }
    }
  }
}

@Composable
private fun BottomActionPanel(
  uiState: ChatDetailUiState,
  viewModel: ChatDetailViewModel,
  mediaLauncher: ActivityResultLauncher<MediaAction>,
  permissionLauncher: ActivityResultLauncher<String>,
  speechRecognitionClient: SpeechRecognitionClient,
  sessionId: String,
  agentId: String?
) {
  val voiceState = uiState.voiceState
  val isVoiceMode = voiceState.isVoiceMode
  val hasText = uiState.chatInputText.isNotBlank()
  val isResponding = uiState.isAiResponding
  val isModelReady = uiState.isModelReady

  Column {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Box(modifier = Modifier.weight(1f)) {
        if (isVoiceMode) {
          VoicePressButton(
            uiState,
            viewModel,
            permissionLauncher,
            speechRecognitionClient
          )
        } else {
          ChatTextField(
            uiState.chatInputText,
            onValueChange = { viewModel.onInputTextChanged(it) })
        }
      }
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = MaterialTheme.spacing.xs),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row {
        MoreActionMenu(mediaLauncher)
        IconButton(
          onClick = { viewModel.toggleInputMode() },
          enabled = !uiState.isEngineLoading && !isResponding,
        ) {
          Icon(
            painter = painterResource(if (isVoiceMode) coreIconR.drawable.core_ic_edit_note else coreIconR.drawable.core_ic_circle_talk),
            contentDescription = "Mode",
            modifier = Modifier.size(MaterialTheme.iconSizes.medium)
          )
        }
      }
      SendOrStopButton(
        isResponding,
        isModelReady,
        hasText || uiState.selectedImages.isNotEmpty(),
        onStop = { viewModel.stopModelResponse() },
        onSend = {
          val paths =
            uiState.selectedImages.filter { !it.isAddButton && !it.path.isNullOrEmpty() }
              .map { it.path!! }
          viewModel.performSendMessage(sessionId, agentId, uiState.chatInputText, paths)
          viewModel.onInputTextChanged("")
        })
    }
  }
}

/**
 * 语音按住说话控制按钮 - 涉及底层 MotionEvent 拦截与分发
 */
@Composable
private fun VoicePressButton(
  uiState: ChatDetailUiState,
  viewModel: ChatDetailViewModel,
  permissionLauncher: ActivityResultLauncher<String>,
  speechClient: SpeechRecognitionClient
) {
  val context = LocalContext.current
  var isPressing by remember { mutableStateOf(false) }
  var touchY by remember { mutableFloatStateOf(0f) }
  val enabled =
    uiState.isModelReady && !uiState.isAiResponding && uiState.voiceState.speechEngineReady

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(MaterialTheme.widgetSizes.inputFieldMinHeight)
      .alpha(if (enabled) 1f else 0.5f)
      .clip(CircleShape)
      .background(if (isPressing) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
      .border(
        MaterialTheme.elevations.stroke,
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        CircleShape
      )
      .pointerInteropFilter { event ->
        if (!enabled) {
          return@pointerInteropFilter false
        }
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
              ) == PackageManager.PERMISSION_GRANTED
            ) {
              isPressing = true
              touchY = event.rawY
              speechClient.start()
            } else {
              permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          }

          MotionEvent.ACTION_MOVE -> {
            val isCanceling =
              (touchY - event.rawY) > ChatDetailDimens.SLIDE_UP_CANCEL_THRESHOLD_PX
            viewModel.updateVoiceCanceling(isCanceling)
          }

          MotionEvent.ACTION_UP -> {
            isPressing = false
            if (uiState.voiceState.isVoiceCanceling) {
              speechClient.cancel()
            } else {
              speechClient.stop()
            }
          }

          MotionEvent.ACTION_CANCEL -> {
            isPressing = false
            speechClient.cancel()
          }
        }
        true
      },
    contentAlignment = Alignment.Center
  ) {
    val progress = uiState.voiceState.speechModelDownloadProgress
    val text = when (progress) {
      VoiceInputUiState.SPEECH_MODEL_PROGRESS_CHECKING -> stringResource(chatR.string.feature_chat_voice_preparing)
      VoiceInputUiState.SPEECH_MODEL_PROGRESS_UNZIPPING -> stringResource(chatR.string.feature_chat_speech_model_unzipping)
      in 0..100 -> stringResource(
        chatR.string.feature_chat_speech_model_download_progress,
        progress
      )

      else -> stringResource(chatR.string.feature_chat_voice_press_to_talk)
    }
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun ChatTextField(value: String, onValueChange: (String) -> Unit) {
  TextField(
    value = value, onValueChange = onValueChange,
    placeholder = {
      Text(
        stringResource(chatR.string.feature_chat_input_hint),
        color = Color.Gray
      )
    },
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = MaterialTheme.widgetSizes.inputFieldMinHeight),
    colors = TextFieldDefaults.colors(
      focusedContainerColor = Color.Transparent,
      unfocusedContainerColor = Color.Transparent,
      disabledContainerColor = Color.Transparent,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
    ),
    trailingIcon = {
      if (value.isNotBlank()) IconButton(
        onClick = { onValueChange("") },
      ) {
        Icon(
          painter = painterResource(coreIconR.drawable.core_ic_cancel),
          contentDescription = "Clear",
          tint = Color.Gray,
          modifier = Modifier.size(MaterialTheme.iconSizes.medium)
        )
      }
    }
  )
}

@Composable
private fun MoreActionMenu(mediaLauncher: ActivityResultLauncher<MediaAction>) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    IconButton(
      onClick = { expanded = true },
    ) {
      Icon(
        painter = painterResource(coreIconR.drawable.core_ic_circle_more),
        contentDescription = "More",
        modifier = Modifier.size(MaterialTheme.iconSizes.medium)
      )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text(stringResource(chatR.string.feature_chat_menu_camera)) },
        onClick = { expanded = false; mediaLauncher.launch(MediaAction.TAKE_PHOTO) },
        leadingIcon = { Icon(painterResource(coreIconR.drawable.core_ic_camera), null) }
      )
      DropdownMenuItem(
        text = { Text(stringResource(chatR.string.feature_chat_menu_photo)) },
        onClick = { expanded = false; mediaLauncher.launch(MediaAction.PICK_IMAGE) },
        leadingIcon = { Icon(painterResource(coreIconR.drawable.core_ic_photo), null) }
      )
    }
  }
}

@Composable
private fun SendOrStopButton(
  responding: Boolean,
  ready: Boolean,
  hasInput: Boolean,
  onStop: () -> Unit,
  onSend: () -> Unit
) {
  if (responding) {
    IconButton(
      onClick = onStop,
    ) {
      Icon(
        painter = painterResource(coreIconR.drawable.core_ic_circle_stop),
        contentDescription = "Stop",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(MaterialTheme.iconSizes.large)
      )
    }
  } else {
    IconButton(
      onClick = onSend,
      enabled = ready && hasInput,
    ) {
      Icon(
        painter = painterResource(coreIconR.drawable.core_ic_circle_send),
        contentDescription = "Send",
        tint = if (ready && hasInput) MaterialTheme.colorScheme.primary else Color.Gray.copy(
          alpha = 0.5f
        ),
        modifier = Modifier.size(MaterialTheme.iconSizes.large)
      )
    }
  }
}

@Composable
private fun VoiceOverlayPanel(voiceState: VoiceInputUiState) {
  AnimatedVisibility(
    visible = voiceState.isVoiceOverlayVisible,
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
          topStart = MaterialTheme.corners.lg,
          topEnd = MaterialTheme.corners.lg
        ),
        color = MaterialTheme.colorScheme.surface
      ) {
        Column(
          modifier = Modifier.padding(MaterialTheme.spacing.xl),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = stringResource(if (voiceState.isVoiceCanceling) chatR.string.feature_chat_voice_release_to_cancel else chatR.string.feature_chat_voice_slide_up_to_cancel),
            style = MaterialTheme.typography.bodyMedium,
            color = if (voiceState.isVoiceCanceling) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
          Box(
            modifier = Modifier
              .size(ChatDetailDimens.voiceOverlayCircleSize)
              .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(coreIconR.drawable.core_ic_audio),
              contentDescription = "Voice",
              modifier = Modifier.size(MaterialTheme.widgetSizes.buttonHeightStandard),
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          val overlayText = voiceState.voiceRecognizedText.ifEmpty {
            stringResource(chatR.string.feature_chat_voice_listening)
          }
          Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
          Text(
            text = overlayText,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

@Composable
private fun LocalImage(
  path: String,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
  adjustViewBounds: Boolean = false
) {
  val context = LocalContext.current
  var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
  LaunchedEffect(path) {
    fetchImage(context, path) { image ->
      bitmap = image
    }
  }
  bitmap?.let {
    val aspectRatio = it.width.toFloat() / it.height.toFloat()
    val imageModifier = if (adjustViewBounds && aspectRatio > 0f) {
      modifier.aspectRatio(aspectRatio)
    } else {
      modifier
    }
    Image(
      bitmap = it.asImageBitmap(),
      contentDescription = null,
      modifier = imageModifier,
      contentScale = contentScale
    )
  } ?: Box(modifier = modifier.background(Color.LightGray))
}

private fun handleSpeechEvent(
  event: SpeechRecognitionEvent,
  viewModel: ChatDetailViewModel,
  sessionId: String,
  agentId: String?,
  context: android.content.Context
) {
  when (event) {
    SpeechRecognitionEvent.Checking -> {
      viewModel.onSpeechEngineChecking()
    }

    is SpeechRecognitionEvent.Downloading -> {
      viewModel.onSpeechEngineDownloading(event.progress)
    }

    SpeechRecognitionEvent.Unzipping -> {
      viewModel.onSpeechEngineUnzipping()
    }

    SpeechRecognitionEvent.EngineReady -> {
      viewModel.onSpeechEngineReady()
    }

    SpeechRecognitionEvent.Started -> {
      viewModel.showVoiceInputUi()
    }

    is SpeechRecognitionEvent.TranscriptChanged -> {
      viewModel.updateVoiceRecognizedText(event.transcript.text)
    }

    is SpeechRecognitionEvent.AudioLevelChanged -> {
      viewModel.updateVoiceRms(event.rmsDB)
    }

    is SpeechRecognitionEvent.Error -> {
      viewModel.finishVoiceInputUi()
    }

    SpeechRecognitionEvent.Canceled -> {
      viewModel.finishVoiceInputUi()
    }

    is SpeechRecognitionEvent.Completed -> {
      viewModel.finishVoiceInputUi()
      val text = event.transcript.text
      if (text.isNotEmpty()) {
        val paths =
          viewModel.uiState.value.selectedImages.filter { !it.isAddButton && !it.path.isNullOrEmpty() }
            .map { it.path!! }
        viewModel.performSendMessage(sessionId, agentId, text, paths)
      } else {
        Toast.makeText(
          context,
          chatR.string.feature_chat_voice_unrecognized,
          Toast.LENGTH_SHORT
        ).show()
      }
    }

    else -> {}
  }
}
