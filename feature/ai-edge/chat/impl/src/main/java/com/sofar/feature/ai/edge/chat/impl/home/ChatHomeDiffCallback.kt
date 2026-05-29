package com.sofar.feature.ai.edge.chat.impl.home

import androidx.recyclerview.widget.DiffUtil
import com.sofar.core.ai.edge.database.entity.SessionEntity

class ChatHomeDiffCallback : DiffUtil.ItemCallback<SessionEntity>() {

  override fun areItemsTheSame(
    oldItem: SessionEntity,
    newItem: SessionEntity
  ): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(
    oldItem: SessionEntity,
    newItem: SessionEntity
  ): Boolean {
    return oldItem == newItem
  }
}