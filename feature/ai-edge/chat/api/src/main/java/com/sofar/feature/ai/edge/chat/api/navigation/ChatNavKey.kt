package com.sofar.feature.ai.edge.chat.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data object ChatNavKey : NavKey

/**
 * 打开已有会话直接构造；新建会话使用 [newSession]。
 *
 * sessionId 刻意不设默认值、也不可空：NavKey 是值对象，Nav3 以其 equals 作为页面身份，
 * 构造函数必须是确定的；随机性只收敛在 [newSession] 这一个入口。
 */
@Serializable
data class ChatDetailNavKey(
  val sessionId: String,
  val agentId: String? = null
) : NavKey {
  companion object {
    /** 新建会话：ID 在此刻生成，并随 NavKey 一起保存，重建 / 进程恢复后保持不变。 */
    fun newSession(agentId: String? = null) =
      ChatDetailNavKey(UUID.randomUUID().toString(), agentId)
  }
}
