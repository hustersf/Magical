package com.sofar.feature.ai.edge.chat.impl.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
import com.sofar.core.common.extension.getParcelableCompat
import com.sofar.core.ui.BaseUIActivity
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import com.sofar.feature.ai.edge.chat.impl.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class ChatDetailActivity : BaseUIActivity() {

  private lateinit var toolbar: MaterialToolbar
  private lateinit var chatEditText: EditText
  private lateinit var cancelBtn: Button
  private lateinit var moreBtn: Button
  private lateinit var talkBtn: Button
  private lateinit var sendBtn: Button
  private lateinit var stopBtn: Button

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ChatDetailAdapter

  private val viewModel: ChatDetailViewModel by viewModels()

  private var currentSessionId: String = ""
  private var agentId: String? = null

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.feature_chat_detail_activity)
    val detailArgs = intent.getParcelableCompat<ChatDetailArgs>(EXTRA_CHAT_DETAIL_ARGS)
    currentSessionId = detailArgs?.sessionId ?: UUID.randomUUID().toString()
    agentId = detailArgs?.agentId
    initView()
    setupListeners()
    initData()
  }

  fun initView() {
    toolbar = findViewById(R.id.chat_detail_toolbar)
    chatEditText = findViewById(R.id.chat_detail_edit_text)
    cancelBtn = findViewById(R.id.chat_detail_cancel_btn)
    moreBtn = findViewById(R.id.chat_detail_more_btn)
    talkBtn = findViewById(R.id.chat_detail_talk_btn)
    sendBtn = findViewById(R.id.chat_detail_send_btn)
    stopBtn = findViewById(R.id.chat_detail_stop_btn)

    recyclerView = findViewById(R.id.chat_detail_list)
    val layoutManager = LinearLayoutManager(this).apply {
      stackFromEnd = true
    }
    recyclerView.layoutManager = layoutManager
    adapter = ChatDetailAdapter()
    recyclerView.adapter = adapter
    val padding = resources.getDimension(R.dimen.core_ui_spacing_lg).toInt()
    recyclerView.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.VERTICAL,
        padding,
        padding
      )
    )
  }


  private fun setupListeners() {
    toolbar.setNavigationOnClickListener {
      finish()
    }

    chatEditText.addTextChangedListener { text ->
      if (viewModel.uiState.value.isAiResponding) return@addTextChangedListener
      val isNotEmpty = !text.isNullOrBlank()
      if (isNotEmpty) {
        cancelBtn.visibility = View.VISIBLE
        sendBtn.visibility = View.VISIBLE
      } else {
        cancelBtn.visibility = View.GONE
        sendBtn.visibility = View.GONE
      }
    }

    cancelBtn.setOnClickListener {
      chatEditText.text?.clear()
    }

    moreBtn.setOnClickListener {
      // TODO: 弹出更多功能面板（照片、文件等）
    }

    talkBtn.setOnClickListener {
      // TODO: 触发对讲/录音功能
    }

    sendBtn.setOnClickListener {
      val message = chatEditText.text.toString().trim()
      if (message.isNotEmpty()) {
        sendMessage(message)
        chatEditText.text?.clear() // 发送后清空
      }
    }

    stopBtn.setOnClickListener {
      viewModel.stopModelResponse()
    }
  }

  fun initData() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        // 启动独立子协程收集 UiState，不阻塞外层主流程
        launch {
          viewModel.uiState.collect { state ->
            renderUiState(state)
          }
        }
        // 启动独立子协程收集 Effect，两个流同时并行并发收集
        launch {
          viewModel.effectFlow.collect { effect ->
            handleEffect(effect)
          }
        }
      }
    }
    viewModel.init(currentSessionId, agentId)
  }

  private fun renderUiState(state: ChatDetailUiState) {
    // A. 实时同步顶部工具栏标题（大模型在说完第一句后会自动覆写重命名该摘要标题）
    toolbar.title = state.sessionTitle.ifEmpty {
      getString(R.string.feature_chat_title_default)
    }

    // B. 数据缝合：将 Room 离线优先推出的最新气泡集合单向提交给你的 ListAdapter
    adapter.submitList(state.messages) {
      if (state.messages.isNotEmpty()) {
        // 如果历史记录发生了位置移动，或者尾部顶出了新气泡，列表始终滑至最底端位置
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return@submitList
        val targetPosition = state.messages.size - 1
        layoutManager.scrollToPositionWithOffset(targetPosition, Int.MIN_VALUE)
      }
    }

    val hasText = !chatEditText.text.isNullOrBlank()

    // C. 控制界面组件置灰锁与思考态：大模型初次加载图或进入首字计算期时，将对应的动作组件加锁
    when {
      // 状态一：底层 C++ 引擎正在读二进制文件、跑神经网络图编译（冷启动/刚进房间/切大模型）
      state.isEngineLoading -> {
        sendBtn.isEnabled = false
        talkBtn.isEnabled = false
        stopBtn.isEnabled = false
      }

      // 状态二：模型未初始化成功
      !state.isModelReady -> {
        sendBtn.isEnabled = false
        talkBtn.isEnabled = false
        stopBtn.visibility = View.GONE
        sendBtn.visibility = if (hasText) View.VISIBLE else View.GONE
      }

      // 状态三：大模型早已 Ready，且目前正在流式输出一句话（高频蹦字生成期）
      state.isAiResponding -> {
        talkBtn.isEnabled = false
        sendBtn.visibility = View.GONE
        stopBtn.visibility = View.VISIBLE
        stopBtn.isEnabled = true
      }

      // 状态四：寂静期、日常常态（AI 没在说话，引擎随时听候调遣）
      else -> {
        stopBtn.visibility = View.GONE
        sendBtn.visibility = if (hasText) View.VISIBLE else View.GONE
        sendBtn.isEnabled = true
        talkBtn.isEnabled = true
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

  private fun sendMessage(text: String) {
    // 🧠 干净利落：彻底消灭 onTextChunksReceived 回调接口，只管发送，不操心刷新
    viewModel.performSendMessage(
      sessionId = currentSessionId,
      agentId = agentId,
      inputTextFieldValue = text,
      sandboxedImagesPath = listOf(), // 预留给 Tab 3
      sandboxedAudioPath = null       // 预留给 Tab 4
    )
  }

  override fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
  }
}