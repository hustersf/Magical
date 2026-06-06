package com.sofar.core.ai.edge.data.entity.agent

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(AgentSourceType.SYSTEM, AgentSourceType.CUSTOM)
annotation class AgentSourceType {
  companion object {
    const val HIDDEN_SYSTEM = -1  // 内置不显示的
    const val SYSTEM = 0 // 官方预设/系统内置
    const val CUSTOM = 1 // 用户自己创建
  }
}