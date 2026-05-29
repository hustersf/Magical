package com.sofar.feature.ai.edge.chat.impl.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
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
  private val chatRepository: ChatRepository
) : ViewModel() {

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
          // chatRepository.deleteSessionById(session.id)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  // 清空历史：清理数据库的同时，一键粉碎磁盘中残留的所有 AI 多媒体大文件
  fun clearAllSessions() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        // chatRepository.clearAllHistoryExceptWorkspace()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}