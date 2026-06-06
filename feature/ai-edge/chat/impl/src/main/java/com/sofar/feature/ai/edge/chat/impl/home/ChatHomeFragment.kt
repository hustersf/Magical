package com.sofar.feature.ai.edge.chat.impl.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
import com.sofar.core.ai.edge.data.entity.chat.ChatPriority
import com.sofar.core.ai.edge.database.entity.SessionEntity
import com.sofar.feature.ai.edge.chat.impl.R
import com.sofar.feature.ai.edge.chat.impl.detail.ChatDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatHomeFragment : Fragment() {

  private val viewModel: ChatHomeViewModel by viewModels()

  private lateinit var toolbar: MaterialToolbar
  private lateinit var chatBtn: ImageButton
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ChatHomeAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.feature_chat_home_fragment, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView(view)
    initData()
  }

  private fun initView(view: View) {
    toolbar = view.findViewById(R.id.chat_home_toolbar)
    chatBtn = view.findViewById(R.id.chat_home_new_btn)
    chatBtn.setOnClickListener {
      ChatDetailActivity.launch(requireContext())
    }
    recyclerView = view.findViewById(R.id.chat_home_list)
    recyclerView.layoutManager = LinearLayoutManager(context)
    adapter = ChatHomeAdapter(
      agentCache = viewModel.agentCache,
      onItemClick = { session, view ->
        ChatDetailActivity.launch(
          requireContext(), ChatDetailArgs(
            sessionId = session.id,
            agentId = session.agentId
          )
        )
      },
      onItemLongClick = { session, view ->
        showSessionLongClickMenu(session)
      })
    recyclerView.adapter = adapter
  }

  private fun initData() {
    // 生命周期感知型数据收集：当界面可见时激活管道，数据库改变时数据自下而上自动秒级渲染
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.sessionListState.collectLatest { sessions ->
          adapter.submitList(sessions)
        }
      }
    }
  }

  private fun showSessionLongClickMenu(session: SessionEntity) {
    val context = context ?: return

    // 区分置顶/普通会话
    val isPinned = session.priority > ChatPriority.NORMAL

    // 1. 动态生成文案队列（置顶会话不允许物理删除，只能清空记录）
    val menuItems = if (isPinned) {
      arrayOf(getString(R.string.feature_chat_action_clear_history)) // "清空对话记录"
    } else {
      arrayOf(
        getString(R.string.feature_chat_action_clear_history), // 索引 0
        getString(R.string.feature_chat_action_delete)        // 索引 1: "删除对话"
      )
    }

    // 2. 展开美观的 Material3 卡片级弹窗
    MaterialAlertDialogBuilder(context)
      .setTitle(session.title.ifEmpty { getString(R.string.feature_chat_title_default) })
      .setItems(menuItems) { dialog, which ->
        if (isPinned) {
          // 🚀 场景 A：置顶会话，无论怎么选都只有这一项，直接触发“清空”
          viewModel.clearChatHistory(session)
        } else {
          // 🚀 场景 B：普通会话，根据索引完美分流
          if (which == 0) {
            viewModel.clearChatHistory(session) // 点了“清空对话记录”
          } else {
            viewModel.deleteSession(session)     // 点了“删除对话”
          }
        }
        dialog.dismiss()
      }
      .show()
  }
}