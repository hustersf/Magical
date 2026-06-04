package com.sofar.core.ai.edge.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 会话历史实体表 —— 对应 Tab 1 (历史记录看板)
 *
 * 【⚙️ 完整架构设计说明书】：
 * 1. 独立轻量单表设计（抗卡顿死穴）：
 *    Tab 1 要求“不论翻译、识图还是会议，只要产生过对话，都在这里按时间倒序排列”。如果直接去聊天消息表里执行
 *    `Group By` 或自连接聚合查询，随着聊天气泡破万，Tab 1 列表在冷启动时会产生灾难性的磁盘 I/O 阻塞导致 UI 严重卡顿。
 *    因此，将每次对话独立为轻量的 [SessionEntity]，列表只需一行极快的单表 SQL 查询，确保高频刷新下的绝对流畅。
 * 2. 局部高频覆写优化：
 *    大模型交互时，会话的更新极其频繁（每次收发字词都会导致时间戳变更）。将时间戳隔离在独立的会话表，
 *    能让写盘动作只发生在轻量单行上，大幅减少磁盘随机写入开销，保护手机闪存寿命。
 * 3. 性能索引设计理由 (indices)：
 *    - Index([priority, updated_at])：多列复合索引。首页执行 `ORDER BY priority DESC, updated_at DESC` 时，
 *      SQLite 能够直接通过 B-Tree 索引硬件级输出完美队列，免去全表内存排序，彻底消除冷启动与滑动的掉帧感。
 *    - Index([agent_id])：必须为外键列显式创建索引，防止 agents 表发生变动时触发全表扫描，引发卡顿或死锁警
 * 4. 外键级联行为设计理由 (ForeignKey)：
 *    关联 [AgentEntity]，配置 [onDelete = SET_NULL]。当用户在 Tab 2 删除了某个自定义智能体人设时，该人设产生过的
 *    历史对话不应该被连带误删，而是让外键自动置空，将会话安全降级为普通自由对话，保障用户单机数据资产安全。
 */
@Entity(
  tableName = "sessions",
  foreignKeys = [
    ForeignKey(
      entity = AgentEntity::class,
      parentColumns = ["id"],
      childColumns = ["agent_id"],
      onDelete = ForeignKey.SET_NULL
    )
  ],
  indices = [
    Index(value = ["priority", "updated_at"]),
    Index(value = ["agent_id"])
  ]
)
data class SessionEntity(
  @PrimaryKey
  val id: String,                                     // 会话全局唯一 ID (建议使用随机 UUID 字符串)
  val title: String,                                  // 会话在 Tab 1 列表显示的标题 (如 "昨晚的会议摘要")
  @ColumnInfo(name = "last_message")
  val lastMessage: String? = null,                    // 会话最后一条消息
  @ColumnInfo(name = "agent_id")
  val agentId: String?,                               // 关联的智能体人设 ID (为 NULL 代表纯大模型自由自由对话)
  val type: String,                                   // 会话多模态来源类型约束 (TEXT: 普通/智能体, VISION: 识图, MEETING: 会议)
  @ColumnInfo(name = "updated_at")
  val updatedAt: Long,                                // 核心置顶指标：最后一条消息交互的毫秒时间戳，用于 Tab 1 降序排列
  val priority: Int = 0                               // 策略级置顶指标：会话优先级数值(越大越靠前)，用于常驻顶部或手动置顶
)