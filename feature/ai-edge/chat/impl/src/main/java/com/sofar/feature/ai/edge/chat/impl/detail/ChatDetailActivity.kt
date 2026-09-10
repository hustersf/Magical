package com.sofar.feature.ai.edge.chat.impl.detail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.common.extension.getParcelableCompat
import com.sofar.core.media.GetMediaContract
import com.sofar.core.media.MediaAction
import com.sofar.core.speech.SpeechRecognitionClient
import com.sofar.core.speech.SpeechRecognitionEvent
import com.sofar.core.speech.SpeechRecognitionRequest
import com.sofar.core.ui.activity.BaseUIActivity
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import com.sofar.feature.ai.edge.chat.impl.R
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageAdapter
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageState
import com.sofar.feature.ai.edge.chat.impl.detail.voice.ChatVoiceOverlayView
import com.sofar.feature.ai.edge.chat.impl.detail.voice.VoiceInputUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import com.sofar.core.res.icon.R as coreIconR
import com.sofar.core.ui.R as coreUiR
import com.sofar.feature.ai.edge.chat.api.R as chatR

@AndroidEntryPoint
class ChatDetailActivity : BaseUIActivity() {

  private lateinit var toolbar: MaterialToolbar
  private lateinit var chatEditText: EditText
  private lateinit var cancelBtn: Button
  private lateinit var moreBtn: Button
  private lateinit var talkBtn: MaterialButton
  private lateinit var sendBtn: Button
  private lateinit var stopBtn: Button
  private lateinit var voicePressBtn: Button
  private lateinit var voiceOverlayView: ChatVoiceOverlayView

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ChatDetailAdapter

  private lateinit var imageRecyclerView: RecyclerView
  private lateinit var imageAdapter: SelectedImageAdapter

  private val viewModel: ChatDetailViewModel by viewModels()

  private var currentSessionId: String = ""
  private var agentId: String? = null

  private lateinit var speechRecognitionClient: SpeechRecognitionClient

  /** 语音按钮的原始文字，首次进入语音模式时捕获，下载结束后恢复 */
  private var voicePressDefaultText: String? = null
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (!isGranted) {
      Toast.makeText(
        this,
        getString(chatR.string.feature_chat_voice_permission_denied),
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  companion object {
    private const val EXTRA_CHAT_DETAIL_ARGS = "extra_chat_detail_args"
    private const val TAG = "ChatDetailActivity"

    @JvmStatic
    fun launch(context: Context, args: ChatDetailArgs? = null) {
      val intent = Intent(context, ChatDetailActivity::class.java).apply {
        putExtra(EXTRA_CHAT_DETAIL_ARGS, args)
        if (context !is Activity) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      }
      context.startActivity(intent)
    }
  }

  private val mediaLauncher = registerForActivityResult(GetMediaContract()) { paths: List<String> ->
    if (paths.isNotEmpty()) {
      handleSelectedImages(paths)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.feature_chat_detail_activity)
    val detailArgs = intent.getParcelableCompat<ChatDetailArgs>(EXTRA_CHAT_DETAIL_ARGS)
    currentSessionId = detailArgs?.sessionId ?: UUID.randomUUID().toString()
    agentId = detailArgs?.agentId
    initView()
    initSpeechRecognitionClient()
    setupListeners()
    initData()
  }

  fun initView() {
    initActionViews()
    initMessageRecyclerView()
    initSelectedImagesRecyclerView()
  }

  private fun initActionViews() {
    toolbar = findViewById(R.id.chat_detail_toolbar)
    chatEditText = findViewById(R.id.chat_detail_edit_text)
    cancelBtn = findViewById(R.id.chat_detail_cancel_btn)
    moreBtn = findViewById(R.id.chat_detail_more_btn)
    talkBtn = findViewById(R.id.chat_detail_talk_btn)
    sendBtn = findViewById(R.id.chat_detail_send_btn)
    stopBtn = findViewById(R.id.chat_detail_stop_btn)
    voicePressBtn = findViewById(R.id.chat_detail_voice_press_btn)
    voiceOverlayView = findViewById(R.id.voice_overlay_container)
  }

  private fun initMessageRecyclerView() {
    recyclerView = findViewById(R.id.chat_detail_list)
    val layoutManager = LinearLayoutManager(this).apply {
      stackFromEnd = true
    }
    recyclerView.layoutManager = layoutManager
    adapter = ChatDetailAdapter()
    recyclerView.adapter = adapter
    val padding = resources.getDimension(coreUiR.dimen.core_ui_spacing_lg).toInt()
    recyclerView.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.VERTICAL,
        padding,
        padding
      )
    )
  }

  private fun initSelectedImagesRecyclerView() {
    imageRecyclerView = findViewById(R.id.chat_detail_image_list)
    imageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    imageAdapter = SelectedImageAdapter(
      onAddClick = { view ->
        showMoreMenu(view)
      },
      onDeleteClick = { imageState ->
        val currentImages = viewModel.uiState.value.selectedImages
        viewModel.updateSelectedImages(currentImages.filterNot { it.path == imageState.path })
      }
    )
    imageRecyclerView.adapter = imageAdapter
    val imagePadding = resources.getDimension(coreUiR.dimen.core_ui_spacing_sm).toInt()
    imageRecyclerView.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.HORIZONTAL,
        imagePadding,
        imagePadding
      )
    )
  }

  private fun initSpeechRecognitionClient() {
    speechRecognitionClient = SpeechRecognitionClient(
      context = this,
      coroutineScope = lifecycleScope,
      request = SpeechRecognitionRequest(
        languageCode = "zh-CN",
        preferOffline = true
      ),
    )
    speechRecognitionClient.prepare()
  }


  private fun setupListeners() {
    toolbar.setNavigationOnClickListener {
      finish()
    }

    chatEditText.addTextChangedListener { text ->
      if (viewModel.uiState.value.isAiResponding) return@addTextChangedListener
      viewModel.onInputTextChanged(text?.toString() ?: "")
    }

    cancelBtn.setOnClickListener {
      chatEditText.text?.clear()
    }

    moreBtn.setOnClickListener { view ->
      showMoreMenu(view)
    }

    talkBtn.setOnClickListener {
      viewModel.toggleInputMode()
    }

    setupVoicePressButton()

    sendBtn.setOnClickListener {
      sendMessage(chatEditText.text?.toString())
    }

    stopBtn.setOnClickListener {
      viewModel.stopModelResponse()
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun setupVoicePressButton() {
    var voiceTouchDownY = 0f
    voicePressBtn.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          if (ContextCompat.checkSelfPermission(
              this,
              Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
          ) {
            voiceTouchDownY = event.rawY
            speechRecognitionClient.start()
          } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
          true
        }

        MotionEvent.ACTION_MOVE -> {
          updateVoiceCancelState(voiceTouchDownY - event.rawY)
          true
        }

        MotionEvent.ACTION_UP -> {
          if (viewModel.uiState.value.voiceState.isVoiceCanceling) speechRecognitionClient.cancel() else speechRecognitionClient.stop()
          true
        }

        MotionEvent.ACTION_CANCEL -> {
          speechRecognitionClient.cancel()
          true
        }

        else -> false
      }
    }
  }

  private fun updateVoiceCancelState(offsetY: Float) {
    val threshold = resources.getDimension(R.dimen.feature_chat_voice_cancel_threshold)
    val shouldCancel = offsetY > threshold
    viewModel.updateVoiceCanceling(shouldCancel)
  }

  private fun handleSpeechRecognitionEvent(event: SpeechRecognitionEvent) {
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
        Log.d(TAG, "voice recognition error(code=${event.error.code}")
        viewModel.finishVoiceInputUi()
      }

      is SpeechRecognitionEvent.Completed -> {
        viewModel.finishVoiceInputUi()
        val text = event.transcript.text
        if (text.isNotEmpty()) {
          sendMessage(text)
        } else {
          Toast.makeText(
            this,
            getString(chatR.string.feature_chat_voice_unrecognized),
            Toast.LENGTH_SHORT
          ).show()
        }
      }

      SpeechRecognitionEvent.Canceled -> {
        viewModel.finishVoiceInputUi()
      }

      else -> {}
    }
  }

  fun initData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          speechRecognitionClient.events.collect { event ->
            handleSpeechRecognitionEvent(event)
          }
        }

        //核心设计：拆分流切片配对 distinctUntilChanged 局部拦截，阻断总状态高频更迭引发的无关UI组件频繁刷新
        //  管道 1：会话标题驱动
        launch {
          viewModel.uiState
            .map { it.sessionTitle }
            .distinctUntilChanged()
            .collect { sessionTitle ->
              renderToolbar(sessionTitle)
            }
        }

        //  管道 2：聊天消息列表驱动（大模型蹦字、音量跳动时自动拦截去重）
        launch {
          viewModel.uiState
            .map { it.messages }
            .distinctUntilChanged()
            .collect { messages ->
              renderMessageList(messages)
            }
        }

        //  管道 3：图片多媒体面板驱动
        launch {
          viewModel.uiState
            .map { it.selectedImages }
            .distinctUntilChanged()
            .collect { selectedImages ->
              renderSelectedImages(selectedImages)
            }
        }

        //  管道 4：底部栏与语音弹窗驱动（打包核心维度，高频蹦字时不触发底栏重绘）
        launch {
          viewModel.uiState
            .map { state ->
              listOf(
                state.voiceState,
                state.chatInputText,
                state.isAiResponding,
                state.isEngineLoading,
                state.isModelReady,
              )
            }
            .distinctUntilChanged()
            .collect {
              renderBottomPanelAndLockState(viewModel.uiState.value)
            }
        }

        //  副作用管道：并发收集 Toast/Alert 弹窗等一次性事件
        launch {
          viewModel.effectFlow.collect { effect ->
            handleEffect(effect)
          }
        }
      }
    }
    viewModel.init(currentSessionId, agentId)
  }


  /**
   * 驱动顶部工具栏标题
   */
  private fun renderToolbar(sessionTitle: String) {
    toolbar.title = sessionTitle.ifEmpty {
      getString(chatR.string.feature_chat_title_default)
    }
  }

  /**
   * 数据单向提交与列表智能自动回滚
   */
  private fun renderMessageList(messages: List<MessageEntity>) {
    adapter.submitList(messages) {
      if (messages.isNotEmpty()) {
        // 如果历史记录发生了位置移动，或者尾部顶出了新气泡，列表始终滑至最底端位置
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@submitList
        val targetPosition = messages.size - 1
        layoutManager.scrollToPositionWithOffset(targetPosition, Int.MIN_VALUE)
      }
    }
  }

  /**
   * 驱动多媒体图片选择器的数据缝合与列表滚动
   */
  private fun renderSelectedImages(selectedImages: List<SelectedImageState>) {
    if (selectedImages.isNotEmpty()) {
      imageRecyclerView.visibility = View.VISIBLE

      // 缝合数据：自动追加末尾的“+”号按钮
      val displayList = selectedImages + SelectedImageState(isAddButton = true)
      imageAdapter.submitList(displayList) {
        val lastPosition = displayList.size - 1
        if (lastPosition >= 0) {
          imageRecyclerView.scrollToPosition(lastPosition)
        }
      }
    } else {
      imageRecyclerView.visibility = View.GONE
      imageAdapter.submitList(emptyList())
    }
  }

  /**
   * 驱动底部控制栏排版、UI组件可见性与状态
   */
  private fun renderBottomPanelAndLockState(state: ChatDetailUiState) {
    val voiceState = state.voiceState
    val isVoiceMode = voiceState.isVoiceMode
    val hasText = state.chatInputText.isNotBlank()
    val isResponding = state.isAiResponding
    val isEngineLoading = state.isEngineLoading
    val isModelReady = state.isModelReady

    voiceOverlayView.render(voiceState)

    // ------------------------------------------
    // UI组件显隐控制
    // ------------------------------------------
    if (isVoiceMode) {
      talkBtn.setIconResource(coreIconR.drawable.core_ic_edit_note)
      chatEditText.visibility = View.INVISIBLE
      voicePressBtn.visibility = View.VISIBLE

      // 语音模式下，文本模式专用的控制按键强制隐藏
      cancelBtn.visibility = View.GONE
      sendBtn.visibility = View.GONE
    } else {
      talkBtn.setIconResource(coreIconR.drawable.core_ic_circle_talk)
      chatEditText.visibility = View.VISIBLE
      voicePressBtn.visibility = View.GONE

      // 发送/清除按钮的显隐：AI 在说话时强行隐藏；AI 寂静常态下，完全由输入框是否有字单向驱动
      val showInputActions = hasText && !isResponding
      val inputBtnVisibility = if (showInputActions) View.VISIBLE else View.GONE
      cancelBtn.visibility = inputBtnVisibility
      sendBtn.visibility = inputBtnVisibility
    }
    // 停止响应按钮的显隐：文本模式下，AI 在流式吐字时展示，常态下隐藏
    stopBtn.visibility = if (isResponding) View.VISIBLE else View.GONE

    // ------------------------------------------
    // UI组件状态控制
    // ------------------------------------------
    // 切换输入模式按钮：只要底层 C++ 引擎在加载，或者 AI 正在说话，就彻底变灰
    talkBtn.isEnabled = !isEngineLoading && !isResponding
    // 按住说话按钮：只有 LLM 模型成功、且 AI 没有在流式输出时，才激活响应
    voicePressBtn.isEnabled = isModelReady && !isResponding && voiceState.speechEngineReady
    voicePressBtn.alpha = if (voicePressBtn.isEnabled) 1f else 0.5f
    // 发送文字按钮：只有当模型初始化成功，且 AI 没有在流式输出时，才激活响应
    sendBtn.isEnabled = isModelReady && !isResponding
    // 停止回答按钮：只有当大模型处于流式思考吐字响应期时，才激活点击响应
    stopBtn.isEnabled = isResponding

    // ------------------------------------------
    // 语音按钮文字：展示 SherpaOnnx 模型下载/解压进度
    // ------------------------------------------
    if (isVoiceMode) {
      // 首次进入语音模式时捕获原始按钮文字，以便下载结束后还原
      if (voicePressDefaultText == null && voicePressBtn.text.isNotEmpty()) {
        voicePressDefaultText = voicePressBtn.text.toString()
      }
      val downloadProgress = state.voiceState.speechModelDownloadProgress
      voicePressBtn.text = when (downloadProgress) {
        VoiceInputUiState.SPEECH_MODEL_PROGRESS_CHECKING -> getString(chatR.string.feature_chat_voice_preparing)
        VoiceInputUiState.SPEECH_MODEL_PROGRESS_UNZIPPING -> getString(chatR.string.feature_chat_speech_model_unzipping)
        in 0..100 -> getString(
          chatR.string.feature_chat_speech_model_download_progress,
          downloadProgress
        )

        else -> voicePressDefaultText ?: voicePressBtn.text
      }
    }
  }

  private fun handleEffect(effect: ChatDetailEffect) {
    when (effect) {
      is ChatDetailEffect.ShowToast -> {
        Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
      }

      is ChatDetailEffect.ShowAlert -> {
        MaterialAlertDialogBuilder(this)
          .setTitle("发生错误")
          .setMessage(effect.message)
          .setCancelable(true)
          .setPositiveButton("确定") { dialog, _ ->
            dialog.dismiss()
          }
          .show()
      }
    }
  }

  private fun sendMessage(text: String?) {
    val trimmedText = text?.trim().orEmpty()

    // 从当前 UiState 中安全提取用户真正选中的本地沙盒图片物理路径列表
    val selectedPaths = viewModel.uiState.value.selectedImages
      .filter { !it.isAddButton && !it.path.isNullOrEmpty() }
      .map { it.path!! }

    // 当“文字不为空”或者“图片列表不为空”时，才允许发送
    if (trimmedText.isNotEmpty() || selectedPaths.isNotEmpty()) {

      // 将文本快照与图片快照，作为原子参数无缝喂给 ViewModel
      viewModel.performSendMessage(
        sessionId = currentSessionId,
        agentId = agentId,
        inputTextFieldValue = trimmedText,
        sandboxedImagesPath = selectedPaths,
        sandboxedAudioPath = emptyList()            // 预留给 Tab 4 录音
      )

      // 确认发起发送后，现场清空 Activity 的输入框文本
      chatEditText.text?.clear()
    }
  }

  private fun handleSelectedImages(paths: List<String>) {
    Log.d(TAG, "收到图片结果，总共选择了 ${paths.size} 张图片")
    val oldImages = viewModel.uiState.value.selectedImages
    val oldPaths = oldImages.mapNotNull { it.path }

    // 多选增量去重
    val newUniquePaths = paths.filter { it !in oldPaths }
    val newStates = newUniquePaths.map { SelectedImageState(path = it, isAddButton = false) }

    viewModel.updateSelectedImages(oldImages + newStates)
  }

  private fun showMoreMenu(anchorView: View) {
    // 创建原生的快捷气泡菜单，直接锚定在 + 号按钮上方或下方弹出
    val popup = PopupMenu(this, anchorView)
    popup.menuInflater.inflate(R.menu.feature_chat_detail_more_menu, popup.menu)
    popup.setForceShowIcon(true)
    // 根据 XML 中定义的 ID 分发点击事件
    popup.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.nav_camera -> mediaLauncher.launch(MediaAction.TAKE_PHOTO)
        R.id.nav_photo -> mediaLauncher.launch(MediaAction.PICK_IMAGE)
      }
      true
    }
    popup.show()
  }

  override fun onDestroy() {
    if (::speechRecognitionClient.isInitialized) {
      speechRecognitionClient.release()
    }
    super.onDestroy()
  }

  override fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
  }
}