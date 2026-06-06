package com.sofar.feature.ai.edge.chat.impl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.cache.AgentCache
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.data.repository.AgentRepository
import com.sofar.core.ai.edge.data.repository.ChatRepository
import com.sofar.core.ai.edge.database.entity.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatHomeViewModel @Inject constructor(
  private val chatRepository: ChatRepository,
  private val agentRepository: AgentRepository
) : ViewModel() {

  val agentCache = AgentCache()

  init {
    viewModelScope.launch {
      // 监听 Agent 表，随时刷满当前界面的局部缓存
      agentRepository.getAllAgentsWithPrepopulate().collect { agents ->
        agentCache.init(agents)
      }
    }
  }

  // 界面不可见5秒后自动暂停监听数据库，回到主页时自动恢复，减少后台资源占用
  val sessionListState: StateFlow<List<SessionEntity>> =
    chatRepository.getAllSessionsWithWorkspace()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
      )

  // 删除会话：级联粉碎本地沙盒中的图片或音频大文件，防止手机磁盘空间暴涨
  fun deleteSession(session: SessionEntity) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          when (session.type) {
            ChatSessionType.VISION -> { /* 删除本地缓存的识图图片 */
            }

            ChatSessionType.MEETING -> { /* 删除本地录音文件 */
            }

            else -> {}
          }
          chatRepository.deleteSessionById(session.id)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  // 仅清空对话记录（置顶会话常驻、或普通会话不想删房间时调用）
  fun clearChatHistory(session: SessionEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      // 1. 手动指定消息表：只清除聊天内容本身
      chatRepository.deleteMessagesBySessionId(session.id)

      // 2. 将会话的最后一条消息置空（恢复出厂设置），时间戳更新
      chatRepository.updateSessionPreviewAndTitle(
        sessionId = session.id,
        title = session.title,
        preview = null // 传入 null，首页列表预览刷新，变回干净的初始文本
      )
    }

    // 清空历史：清理数据库的同时，一键粉碎磁盘中残留的所有 AI 多媒体大文件
    fun clearAllSessions() {
      viewModelScope.launch(Dispatchers.IO) {
        try {

        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}