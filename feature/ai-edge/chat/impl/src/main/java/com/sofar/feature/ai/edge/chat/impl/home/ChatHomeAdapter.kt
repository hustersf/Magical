package com.sofar.feature.ai.edge.chat.impl.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.chat.ChatSessionType
import com.sofar.core.ai.edge.database.entity.SessionEntity
import com.sofar.feature.ai.edge.chat.impl.R

class ChatHomeAdapter(
  private val onItemClick: (SessionEntity, View) -> Unit,
  private val diffCallback: ChatHomeDiffCallback = ChatHomeDiffCallback()
) : ListAdapter<SessionEntity, ChatHomeViewHolder>(diffCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHomeViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.feature_chat_home_adapter_item, parent, false)
    return ChatHomeViewHolder(itemView, onItemClick)
  }

  override fun onBindViewHolder(holder: ChatHomeViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item)
  }

}

class ChatHomeViewHolder(
  itemView: View,
  private val onItemClick: (SessionEntity, View) -> Unit
) : RecyclerView.ViewHolder(itemView) {

  private val avatarTv: TextView = itemView.findViewById(R.id.chat_avatar_tv)
  private val titleTv: TextView = itemView.findViewById(R.id.chat_title_tv)
  private val previewTv: TextView = itemView.findViewById(R.id.chat_preview_tv)

  fun bind(item: SessionEntity) {
    itemView.setOnClickListener { onItemClick(item, itemView) }
    titleTv.text = item.title

    var targetColorRes = R.color.feature_chat_home_avatar_pink
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
          avatarTv.text = "🤖"
          previewTv.text = "智能体对话中..."
          targetColorRes = R.color.feature_chat_home_avatar_yellow
        }
      }

      else -> {
        avatarTv.text = "✉️"
        previewTv.text = "继续对话"
      }
    }
    val targetColor = ContextCompat.getColor(itemView.context, targetColorRes)
    val backgroundDrawable = avatarTv.background as? GradientDrawable
    backgroundDrawable?.setColor(targetColor)
  }
}