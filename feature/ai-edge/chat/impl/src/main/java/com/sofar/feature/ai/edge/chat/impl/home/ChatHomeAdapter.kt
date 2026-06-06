package com.sofar.feature.ai.edge.chat.impl.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.cache.AgentCache
import com.sofar.core.ai.edge.data.entity.chat.ChatPriority
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.database.entity.SessionEntity
import com.sofar.feature.ai.edge.chat.impl.R

class ChatHomeAdapter(
  private val agentCache: AgentCache,
  private val onItemClick: (SessionEntity, View) -> Unit,
  private val onItemLongClick: (SessionEntity, View) -> Unit,
  private val diffCallback: ChatHomeDiffCallback = ChatHomeDiffCallback()
) : ListAdapter<SessionEntity, ChatHomeViewHolder>(diffCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHomeViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.feature_chat_home_adapter_item, parent, false)
    return ChatHomeViewHolder(itemView, agentCache, onItemClick, onItemLongClick)
  }

  override fun onBindViewHolder(holder: ChatHomeViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item)
  }

}

class ChatHomeViewHolder(
  itemView: View,
  private val agentCache: AgentCache,
  private val onItemClick: (SessionEntity, View) -> Unit,
  private val onItemLongClick: (SessionEntity, View) -> Unit
) : RecyclerView.ViewHolder(itemView) {

  private val avatarTv: TextView = itemView.findViewById(R.id.chat_avatar_tv)
  private val titleTv: TextView = itemView.findViewById(R.id.chat_title_tv)
  private val previewTv: TextView = itemView.findViewById(R.id.chat_preview_tv)
  private val tagTv: TextView = itemView.findViewById(R.id.chat_tag_tv)

  fun bind(item: SessionEntity) {
    itemView.setOnClickListener { onItemClick(item, itemView) }
    itemView.setOnLongClickListener {
      onItemLongClick(item, itemView)
      true
    }
    titleTv.text = item.title.ifEmpty {
      titleTv.context.getString(R.string.feature_chat_title_default)
    }

    var targetColorRes = R.color.feature_chat_home_avatar_pink
    tagTv.visibility = View.GONE
    when (item.type) {
      ChatSessionType.VISION -> {
        avatarTv.text = "🖼️"
        previewTv.text = "[图片分析] 点击查看识图详情"
        targetColorRes = R.color.feature_chat_home_avatar_blue
      }

      ChatSessionType.MEETING -> {
        avatarTv.text = "🎙️"
        previewTv.text = "[会议摘要] 点击查看录音摘要"
        targetColorRes = R.color.feature_chat_home_avatar_green
      }

      ChatSessionType.TEXT -> {
        if (item.agentId.isNullOrEmpty()) {
          avatarTv.text = "💬"
          previewTv.text = "点击继续自由对话"
          targetColorRes = R.color.feature_chat_home_avatar_pink
        } else {
          val agent = agentCache.get(item.agentId)
          avatarTv.text = agent?.avatar ?: "🤖"
          previewTv.text = "智能体对话中..."
          targetColorRes = R.color.feature_chat_home_avatar_agent
          agent?.name?.let {
            tagTv.visibility = View.VISIBLE
            tagTv.text = it
          }
        }
      }

      else -> {
        avatarTv.text = "✉️"
        previewTv.text = "继续对话"
      }
    }

    item.lastMessage?.let {
      previewTv.text = it
    }
    itemView.isActivated = item.priority > ChatPriority.NORMAL
    val targetColor = ContextCompat.getColor(itemView.context, targetColorRes)
    val backgroundDrawable = avatarTv.background?.mutate() as? GradientDrawable
    backgroundDrawable?.setColor(targetColor)
  }
}