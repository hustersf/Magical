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
import com.sofar.core.ai.edge.data.entity.chat.ChatDetailArgs
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
      onItemClick = { session, view ->
        ChatDetailActivity.launch(
          requireContext(), ChatDetailArgs(
            sessionId = session.id,
            agentId = session.agentId
          )
        )
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
}