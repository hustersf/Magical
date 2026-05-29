package com.sofar.core.ai.edge.data.repository

import com.sofar.core.ai.edge.database.dao.AgentDao
import com.sofar.core.ai.edge.database.entity.AgentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class AgentRepository(private val agentDao: AgentDao) {

  fun getAllAgentsWithPrepopulate(): Flow<List<AgentEntity>> {
    return agentDao.getAllAgents()
      .onStart { ensureDefaultAgentsExist() } // 🎯 核心保底：无感检查并初始化默认智能体
      .flowOn(Dispatchers.IO)
  }

  /**
   * 确保基础默认人设安全落库
   */
  private suspend fun ensureDefaultAgentsExist() = withContext(Dispatchers.IO) {
    // 如果数据库刚创建，内置智能体数量为 0
    if (agentDao.getAgentCount() == 0) {
      val defaultAgents = listOf(
        AgentEntity(
          id = "english_coach",
          name = "英语口语教练", // 未来可以极方便地改为动态国际化字符串
          avatar = "🇬🇧",
          systemPrompt = "You are a patient English coach. Correct my grammar mistakes and keep the conversation fun."
        ),
        AgentEntity(
          id = "translator",
          name = "同声传译官",
          avatar = "🗣️",
          systemPrompt = "你是一个精通多国语言的翻译官。请直接将用户输入的所有非英文内容翻译成英文，英文内容翻译成中文，不要输出任何额外的解释。"
        )
      )
      agentDao.insertAgents(defaultAgents)
    }
  }

}