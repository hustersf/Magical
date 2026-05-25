package com.sofar.core.ai.edge.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 智能体配置实体表 —— 对应 Tab 2 (发现中心) 的核心业务数据源
 *
 * 【⚙️ 完整架构设计说明书】：
 * 1. 业务解耦与性格动态注入：
 *    本表的核心灵魂在于持久化存储 [systemPrompt]（系统指令）。Google AI Edge (如 MediaPipe GenAI Tasks API)
 *    在手机本地拉起大模型（如 Gemma 2）时，允许传入 `system_instruction` 参数。将人设提示词存入数据库，
 *    上层业务（ViewModel）就能在用户点击 Tab 2 某个人设开启对话时，动态提取并注入给模型，让模型瞬间改变回答作风。
 * 2. 结合 Tab 5 的多模型动态调度：
 *    [modelId] 字段是针对 Tab 5 (模型后台管理) 埋下的深度伏笔。在端侧 AI 演进中，针对“图像理解”需要加载
 *    多模态视觉模型，针对“高效翻译”需要加载轻量化小文本模型。此字段允许不同的人设智能体在初始化时，
 *    在底层自动调度、匹配并加载不同的本地物理 `.tflite` 或 `.bin` 模型文件，实现“专模专用”。
 */
@Entity(tableName = "agents")
data class AgentEntity(
  @PrimaryKey val id: String, // 唯一标识，如 "english_coach"
  val name: String,           // 显示名称，如 "英语口语助手"
  val avatar: String,         // 头像路径或 Emoji 图标
  val systemPrompt: String,   // 喂给 Google AI Edge 的系统提示词
  val modelId: String? = null // 绑定的特定模型 ID
)

