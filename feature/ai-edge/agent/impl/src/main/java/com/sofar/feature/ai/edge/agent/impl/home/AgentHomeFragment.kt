package com.sofar.feature.ai.edge.agent.impl.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.sofar.core.ai.edge.data.entity.agent.AgentSourceType
import com.sofar.core.ai.edge.database.entity.AgentEntity
import com.sofar.feature.ai.edge.agent.impl.R
import com.sofar.feature.ai.edge.chat.api.ChatNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgentHomeFragment : Fragment() {

  @Inject
  lateinit var chatNavigator: ChatNavigator

  private val viewModel: AgentHomeViewModel by viewModels()

  private lateinit var toolbar: MaterialToolbar
  private lateinit var addBtn: ImageButton
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: AgentHomeAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.feature_agent_home_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView(view)
    initData()
  }

  private fun initView(view: View) {
    toolbar = view.findViewById(R.id.agent_home_toolbar)
    addBtn = view.findViewById(R.id.agent_home_add_btn)
    recyclerView = view.findViewById(R.id.agent_home_list)
    recyclerView.layoutManager = LinearLayoutManager(context)
    adapter = AgentHomeAdapter(
      onItemClick = { agent ->
        onAgentSelected(agent)
      },
      onItemLongClick = { agent ->
        showAgentMenuDialog(agent)
      }
    )
    recyclerView.adapter = adapter
    addBtn.setOnClickListener {
      showAgentEditorDialog()
    }
  }

  private fun initData() {
    // 生命周期感知型数据收集：当界面可见时激活管道，数据库改变时数据自下而上自动秒级渲染
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          viewModel.agentListState.collectLatest { agents ->
            adapter.submitList(agents)
          }
        }
        launch {
          viewModel.messageFlow.collectLatest { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
  }

  private fun showAgentDetailDialog(agent: AgentEntity) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(agent.name)
      .setMessage(
        getString(
          R.string.feature_agent_detail_message,
          agent.avatar,
          agent.modelId ?: getString(R.string.feature_agent_model_unbound),
          agent.systemPrompt
        )
      )
      .setPositiveButton(R.string.feature_agent_action_chat) { dialog, _ ->
        dialog.dismiss()
        onAgentSelected(agent)
      }
      .setNeutralButton(R.string.feature_agent_action_edit) { dialog, _ ->
        dialog.dismiss()
        showAgentEditorDialog(agent)
      }
      .setNegativeButton(R.string.feature_agent_action_delete) { dialog, _ ->
        dialog.dismiss()
        showDeleteConfirmationDialog(agent)
      }
      .show()
  }

  private fun showAgentMenuDialog(agent: AgentEntity) {
    // 官方内置智能体不可被编辑和删除
    if (agent.sourceType == AgentSourceType.SYSTEM) {
      return
    }

    val options = arrayOf(
      getString(R.string.feature_agent_action_edit),
      getString(R.string.feature_agent_action_delete)
    )

    MaterialAlertDialogBuilder(requireContext())
      .setTitle(agent.name)
      .setItems(options) { dialog, which ->
        dialog.dismiss()
        when (which) {
          0 -> showAgentEditorDialog(agent)       // 点击“编辑”，带入旧实体数据回显
          1 -> showDeleteConfirmationDialog(agent) // 点击“删除”，呼出二次确认弹窗
        }
      }
      .show()
  }

  private fun showAgentEditorDialog(agent: AgentEntity? = null) {
    val dialogView = layoutInflater.inflate(R.layout.feature_agent_editor_dialog, null, false)
    val avatarEt = dialogView.findViewById<TextInputEditText>(R.id.agent_editor_avatar_et)
    val nameEt = dialogView.findViewById<TextInputEditText>(R.id.agent_editor_name_et)
    val promptEt = dialogView.findViewById<TextInputEditText>(R.id.agent_editor_prompt_et)

    avatarEt.setText(agent?.avatar ?: "🤖")
    nameEt.setText(agent?.name.orEmpty())
    promptEt.setText(
      agent?.systemPrompt ?: resources.getString(R.string.feature_agent_default_prompt_content)
    )

    val dialog = MaterialAlertDialogBuilder(requireContext())
      .setTitle(if (agent == null) R.string.feature_agent_create_title else R.string.feature_agent_edit_title)
      .setView(dialogView)
      .setNegativeButton(R.string.feature_agent_action_cancel, null)
      .setPositiveButton(
        if (agent == null) R.string.feature_agent_action_create else R.string.feature_agent_action_save,
        null
      )
      .create()

    dialog.setOnShowListener {
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val handled = if (agent == null) {
          viewModel.createAgent(
            name = nameEt.text?.toString().orEmpty(),
            avatar = avatarEt.text?.toString().orEmpty(),
            systemPrompt = promptEt.text?.toString().orEmpty(),
          )
        } else {
          viewModel.updateAgent(
            id = agent.id,
            name = nameEt.text?.toString().orEmpty(),
            avatar = avatarEt.text?.toString().orEmpty(),
            systemPrompt = promptEt.text?.toString().orEmpty(),
          )
        }
        if (handled) {
          dialog.dismiss()
        }
      }
    }
    dialog.show()
  }

  private fun showDeleteConfirmationDialog(agent: AgentEntity) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(R.string.feature_agent_delete_title)
      .setMessage(getString(R.string.feature_agent_delete_message, agent.name))
      .setPositiveButton(R.string.feature_agent_action_delete) { dialog, _ ->
        viewModel.deleteAgent(agent)
        dialog.dismiss()
      }
      .setNegativeButton(R.string.feature_agent_action_cancel, null)
      .show()
  }

  private fun onAgentSelected(agent: AgentEntity) {
    chatNavigator.launchChatDetail(requireContext(), agentId = agent.id)
  }
}