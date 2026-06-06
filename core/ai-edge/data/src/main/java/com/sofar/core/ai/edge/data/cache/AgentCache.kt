package com.sofar.core.ai.edge.data.cache

import com.sofar.core.ai.edge.database.entity.AgentEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * 局部智能体内存缓存 —— 实例生命周期完全绑定宿主 ViewModel
 */
class AgentCache {
  private val cache = ConcurrentHashMap<String, AgentEntity>()

  companion object {
    const val BABY_KELE_ID = "workspace_baby_kele"
  }

  fun init(allAgents: List<AgentEntity>) {
    cache.clear()
    allAgents.forEach { cache[it.id] = it }
  }

  fun get(agentId: String?): AgentEntity? {
    if (agentId.isNullOrEmpty()) return null
    return cache[agentId]
  }
}