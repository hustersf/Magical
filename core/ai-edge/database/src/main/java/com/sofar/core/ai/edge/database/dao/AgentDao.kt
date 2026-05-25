package com.sofar.core.ai.edge.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofar.core.ai.edge.database.entity.AgentEntity
import kotlinx.coroutines.flow.Flow

/**
 * 智能体数据中心接口 —— 支撑 Tab 2 (发现中心)
 */
@Dao
interface AgentDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAgents(agents: List<AgentEntity>)

  @Query("SELECT * FROM agents")
  fun getAllAgents(): Flow<List<AgentEntity>> // 响应式冷流：Tab 2 界面可见时才按需监听

  @Query("SELECT * FROM agents WHERE id = :agentId")
  suspend fun getAgentById(agentId: String): AgentEntity? // 单次挂起反查：用于新开老会话时加载模型记忆
}