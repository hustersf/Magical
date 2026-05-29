package com.sofar.core.ai.edge.data.repository

import com.sofar.core.ai.edge.data.entity.chat.ChatPriority
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.database.dao.SessionDao
import com.sofar.core.ai.edge.database.entity.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(private val sessionDao: SessionDao) {
  /**
   * 提供给首页的真实数据流
   * 🎯 使用 onStart 拦截：在首页刚开始监听数据的一瞬间，以最高的优先级确保置顶框的存在
   */
  fun getAllSessionsWithWorkspace(): Flow<List<SessionEntity>> {
    return sessionDao.getAllSessions()
      .onStart { ensureWorkspaceExist() } // 核心保底：无感检查并初始化置顶框
      .flowOn(Dispatchers.IO) // 强制切到 I/O 线程，杜绝主线程卡顿
  }

  /**
   * 确保数据库里至少存在一个置顶的工作区
   */
  private suspend fun ensureWorkspaceExist() = withContext(Dispatchers.IO) {
    // 检查当前数据库中是否已经有置顶的常驻对话框
    val hasWorkspace = sessionDao.getWorkspaceCount(ChatPriority.WORKSPACE) > 0
    if (!hasWorkspace) {
      // 如果是初次冷启动（用户刚下载 App），原地在数据库中正式持久化一条置顶主会话
      val defaultWorkspace = SessionEntity(
        id = UUID.randomUUID().toString(),
        title = "✨ 发起新对话...",
        agentId = null,
        type = ChatSessionType.TEXT,
        updatedAt = System.currentTimeMillis(),
        priority = ChatPriority.WORKSPACE
      )
      sessionDao.insertSession(defaultWorkspace)
    }
  }
}