package com.sofar.feature.ai.edge.agent.impl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.agent.AgentSourceType
import com.sofar.core.ai.edge.data.repository.AgentRepository
import com.sofar.core.ai.edge.database.entity.AgentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class AgentHomeViewModel @Inject constructor(
  private val agentRepository: AgentRepository
) : ViewModel() {

  private val _messageFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
  val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

  // 智能体列表状态：网格展示所有可用的智能体
  // 界面不可见5秒后自动暂停监听数据库，回到主页时自动恢复，减少后台资源占用
  val agentListState: StateFlow<List<AgentEntity>> =
    agentRepository.getAllAgentsWithPrepopulate()
      .map { allAgents ->
        // 只留可见的
        allAgents.filter { it.sourceType != AgentSourceType.HIDDEN_SYSTEM }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
      )

  fun createAgent(
    name: String,
    avatar: String,
    systemPrompt: String,
    modelId: String
  ): Boolean {
    val agent = buildValidatedAgent(
      id = UUID.randomUUID().toString(),
      name = name,
      avatar = avatar,
      systemPrompt = systemPrompt,
      modelId = modelId
    ) ?: return false
    viewModelScope.launch {
      runCatching {
        agentRepository.createAgent(agent)
      }.onSuccess {
        _messageFlow.emit("智能体已创建")
      }.onFailure {
        _messageFlow.emit("创建失败，请稍后重试")
      }
    }
    return true
  }

  fun updateAgent(
    id: String,
    name: String,
    avatar: String,
    systemPrompt: String,
    modelId: String
  ): Boolean {
    val agent = buildValidatedAgent(
      id = id,
      name = name,
      avatar = avatar,
      systemPrompt = systemPrompt,
      modelId = modelId
    ) ?: return false
    viewModelScope.launch {
      runCatching {
        agentRepository.updateAgent(agent)
      }.onSuccess {
        _messageFlow.emit("智能体已更新")
      }.onFailure {
        _messageFlow.emit("更新失败，请稍后重试")
      }
    }
    return true
  }

  fun deleteAgent(agent: AgentEntity) {
    viewModelScope.launch {
      runCatching {
        agentRepository.deleteAgent(agent.id)
      }.onSuccess {
        _messageFlow.emit("已删除智能体：${agent.name}")
      }.onFailure {
        _messageFlow.emit("删除失败，请稍后重试")
      }
    }
  }

  private fun buildValidatedAgent(
    id: String,
    name: String,
    avatar: String,
    systemPrompt: String,
    modelId: String
  ): AgentEntity? {
    val normalizedName = name.trim()
    if (normalizedName.isEmpty()) {
      _messageFlow.tryEmit("请输入智能体名称")
      return null
    }
    val normalizedPrompt = systemPrompt.trim()
    if (normalizedPrompt.isEmpty()) {
      _messageFlow.tryEmit("请输入系统提示词")
      return null
    }
    val normalizedAvatar = avatar.trim().ifEmpty { "🤖" }
    val normalizedModelId = modelId.trim().ifEmpty { null }
    return AgentEntity(
      id = id,
      name = normalizedName,
      avatar = normalizedAvatar,
      systemPrompt = normalizedPrompt,
      modelId = normalizedModelId
    )
  }
}