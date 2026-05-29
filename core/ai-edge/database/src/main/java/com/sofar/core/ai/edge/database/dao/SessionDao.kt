package com.sofar.core.ai.edge.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofar.core.ai.edge.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 会话历史管理接口 —— 支撑 Tab 1 (历史看板列表)
 */
@Dao
interface SessionDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: SessionEntity)

  @Query("SELECT * FROM sessions ORDER BY priority DESC, updated_at DESC")
  fun getAllSessions(): Flow<List<SessionEntity>> // 响应式倒序流：数据库写盘后自动推流实现列表置顶

  @Query("SELECT COUNT(*) FROM sessions WHERE priority = :workspacePriority")
  suspend fun getWorkspaceCount(workspacePriority: Int): Int

  @Query("SELECT * FROM sessions WHERE id = :sessionId")
  suspend fun getSessionById(sessionId: String): SessionEntity? // 单次挂起：用于原子事务分支校验

  @Query("DELETE FROM sessions WHERE id = :sessionId")
  suspend fun deleteSessionById(sessionId: String) // 单次挂起：删除会话，联动级联清空底层消息

  @Query("UPDATE sessions SET updated_at = :time, title = :title WHERE id = :sessionId")
  suspend fun updateSessionTimeAndTitle(sessionId: String, time: Long, title: String) // 增量覆写修改时序
}