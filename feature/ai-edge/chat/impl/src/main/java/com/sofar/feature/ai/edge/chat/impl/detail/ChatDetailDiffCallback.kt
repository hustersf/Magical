package com.sofar.feature.ai.edge.chat.impl.detail

import androidx.recyclerview.widget.DiffUtil
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ai.edge.database.entity.SessionEntity

class ChatDetailDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {

  override fun areItemsTheSame(
    oldItem: MessageEntity,
    newItem: MessageEntity
  ): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(
    oldItem: MessageEntity,
    newItem: MessageEntity
  ): Boolean {
    return oldItem == newItem
  }
}