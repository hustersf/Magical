package com.sofar.core.ai.edge.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofar.core.ai.edge.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息气泡详情接口 —— 支撑 Tab 1 详情、Tab 3 识图及 Tab 4 会议
 */
@Dao
interface MessageDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity) // 增量写入：AI引擎高频Stream蹦字时复写此方法

  @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at ASC")
  fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>> // 响应式正序流：配合 View 层 ListAdapter 渲染打字机特效

  @Query("DELETE FROM messages WHERE session_id = :sessionId")
  suspend fun clearMessagesBySession(sessionId: String) // 物理删除：用于重置本地模型的上下文 Token 视窗

  @Query("DELETE FROM messages WHERE id = :messageId")
  suspend fun deleteMessageById(messageId: String)

  @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY created_at DESC LIMIT :limit")
  suspend fun getRecentMessages(sessionId: String, limit: Int): List<MessageEntity>
}