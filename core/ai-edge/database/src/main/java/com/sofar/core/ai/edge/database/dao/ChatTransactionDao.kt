package com.sofar.core.ai.edge.database.dao

import androidx.room.Dao
import androidx.room.Transaction
import com.sofar.core.ai.edge.database.entity.MessageEntity

/**
 * 跨表复合本地事务接口 —— 供 Repository 数据层调用
 */
@Dao
abstract class ChatTransactionDao : SessionDao, MessageDao {

  /**
   * 复合本地事务：
   * 用户或 AI 发送新消息时，往 messages 表插入记录，同时自动更新对应 session 的最后活跃时间并置顶。
   */
  @Transaction
  open suspend fun sendMessageAndUpsertSession(message: MessageEntity, sessionTitle: String) {
    // 1. 插入消息记录 (来自 MessageDao)
    insertMessage(message)

    // 2. 检查会话是否存在，存在则更新 (来自 SessionDao)
    val existingSession = getSessionById(message.sessionId)

    if (existingSession != null) {
      updateSessionTimeAndTitle(
        sessionId = message.sessionId,
        time = message.createdAt,
        title = sessionTitle
      )
    }
  }
}