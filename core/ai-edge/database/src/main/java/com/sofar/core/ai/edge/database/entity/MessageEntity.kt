package com.sofar.core.ai.edge.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 聊天消息详情表 —— 支撑 Tab 1 对话详情页、Tab 3 识图输入流、Tab 4 会议落库的底层核心数据载体
 *
 * 【⚙️ 完整架构设计说明书】：
 * 1. 严禁多媒体二进制入库（单机闪退防御）：
 *    手机相机拍照(Tab 3)产生的图片通常有 4MB，会议长时间录音(Tab 4)产生的音频动辄数十MB。如果将 these 文件的二进制
 *    byte[] 数组作为 BLOB 直接插进 SQLite 数据库，Room 在读取时会瞬间挤爆 Android 系统的 CursorWindow 内存（标准限制仅几MB），
 *    引发严重的内存泄漏或 App 直接闪退。本表采用 [filePath] 仅记录多媒体在手机沙盒私有目录中的物理路径，实现多模态数据的轻量持久化。
 * 2. 打字机流式（Stream）响应式复写：
 *    端侧大模型在进行本地推理时，输出是“流式”一个词一个词往外蹦的。AI 推理层会高频、连续地调用复写 [textContent] 字段。
 *    配合 Room 自动为本表创建的 SQLite 底层临时触发器（Trigger），任何增量复写都会瞬间向应用层发射刷新信号，
 *    无缝驱动普通 View 系统进行精确的局部增量重绘，完美渲染出丝滑的打字机特效。
 * 3. 性能与外键双重索引设计理由 (indices)：
 *    - 显式为外键列 [session_id] 创建了唯一必加索引。
 *    - 业务加速理由：聊天详情页启动或刷新时，会高频执行 `WHERE session_id = :id` 查询。索引让该查询时间复杂度从全表 O(N) 降为 B-Tree 的 O(logN)。
 *    - 崩溃防御理由：当外键触发级联删除时，SQLite 借助此索引可以直接定位并抹除对应行。如果没有这个索引，删除一个会话会导致
 *      SQLite 强行对百万级消息记录做全表扫描，手机会瞬间卡死数秒并直接引发系统的 ANR（应用无响应）闪退。
 * 4. 外键级联行为设计理由 (ForeignKey)：
 *    强关联 [SessionEntity]，配置 [onDelete = CASCADE]（级联删除）。当用户在 Tab 1 看板删除某条历史会话时，
 *    底层数据库自动级联抹除 messages 表中属于该会话的所有气泡碎片，不留单机垃圾数据，实现完美的物理文件生命周期闭环。
 */
@Entity(
  tableName = "messages",
  foreignKeys = [
    ForeignKey(
      entity = SessionEntity::class,
      parentColumns = ["id"],
      childColumns = ["session_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [
    Index(value = ["session_id"])
  ]
)
data class MessageEntity(
  @PrimaryKey
  val id: String,                             // 消息全局唯一 ID (UUID)
  @ColumnInfo(name = "session_id")
  val sessionId: String,                      // 归属的父级会话 ID
  val role: String,                           // 消息发送者角色类型 (取值约束: "user" 或 "assistant")
  @ColumnInfo(name = "content_type")
  val contentType: String,                    // 消息内容多模态分类 (取值约束: "text", "image", "audio")
  @ColumnInfo(name = "text_content")
  val textContent: String?,                   // 文本气泡内容载体 (大模型打字机吐字时高频复写覆盖此字段)
  @ColumnInfo(name = "file_path")
  val filePath: String?,                      // 崩溃防御核心：指向 Tab 3 拍照图片或 Tab 4 录音文件在手机沙盒中的物理物理路径
  @ColumnInfo(name = "created_at")
  val createdAt: Long                         // 气泡流时序排序指标：单页面内聊天气泡正序排列依据 (毫秒时间戳)
)