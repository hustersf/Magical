package com.sofar.feature.ai.edge.chat.impl.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageRole
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageType
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.common.extension.getParcelableCompat
import com.sofar.core.ui.BaseUIActivity
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import com.sofar.feature.ai.edge.chat.impl.R
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class ChatDetailActivity : BaseUIActivity() {

  private lateinit var toolbar: MaterialToolbar
  private lateinit var chatEditText: EditText
  private lateinit var cancelBtn: Button
  private lateinit var moreBtn: Button
  private lateinit var talkBtn: Button
  private lateinit var sendBtn: Button

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ChatDetailAdapter

  companion object {
    private const val EXTRA_CHAT_DETAIL_ARGS = "extra_chat_detail_args"

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
  }

  fun initData() {
    val sessionId = UUID.randomUUID().toString()
    val currentTime = System.currentTimeMillis()

    val msg1 = MessageEntity(
      id = UUID.randomUUID().toString(),
      sessionId = sessionId,
      role = ChatMessageRole.USER,
      contentType = ChatMessageType.TEXT,
      textContent = "皮疹能擦保湿霜吗？",
      filePath = null,
      createdAt = currentTime - 60000 // 1分钟前
    )

    val msg2 = MessageEntity(
      id = UUID.randomUUID().toString(),
      sessionId = sessionId,
      role = ChatMessageRole.ASSISTANT,
      contentType = ChatMessageType.TEXT,
      textContent = "皮疹可以涂保湿霜，而且是基础护理首选。\n\n1、日常用法\n• 无破溃流水：一天2~3次厚涂，建立皮肤屏障。\n• 伴随红肿痒：建议搭配炉甘石洗剂，半小时后再涂保湿霜。",
      filePath = null,
      createdAt = currentTime - 30000 // 30秒前
    )

    val msg3 = MessageEntity(
      id = UUID.randomUUID().toString(),
      sessionId = sessionId,
      role = ChatMessageRole.USER,
      contentType = ChatMessageType.TEXT,
      textContent = "那如果有破损流水的严重情况呢？那如果有破损流水的严重情况呢？那如果有破损流水的严重情况呢？那如果有破损流水的严重情况呢？那如果有破损流水的严重情况呢？",
      filePath = null,
      createdAt = currentTime - 10000 // 10秒前
    )

    // 4. 💎 重点设计：模拟发送动作后，立刻顶出的“AI正在思考...”灰色气泡
    val msgStreamingAi = MessageEntity(
      id = UUID.randomUUID().toString(),
      sessionId = sessionId,
      role = ChatMessageRole.ASSISTANT,
      contentType = ChatMessageType.TEXT,
      textContent = null, // 💡 故意传入 null！触发我们刚刚在 AiTextViewHolder 里面写的“AI 正在思考...”兜底字样
      filePath = null,
      createdAt = currentTime
    )

    // 5. 全量打包推入内存池，并交付给 ListAdapter 引擎渲染首屏 UI
    val mockMessagesList = mutableListOf<MessageEntity>()
    mockMessagesList.addAll(listOf(msg1, msg2, msg3, msgStreamingAi))
    adapter.submitList(mockMessagesList)
  }

  private fun sendMessage(text: String) {
    // TODO: 处理发送逻辑
  }

  override fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
  }
}