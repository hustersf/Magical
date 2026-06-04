package com.sofar.feature.ai.edge.chat.impl.detail

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.sofar.core.ai.edge.database.entity.MessageEntity

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

  override fun getChangePayload(oldItem: MessageEntity, newItem: MessageEntity): Any? {
    if (oldItem.textContent != newItem.textContent) {
      return Bundle().apply {
        // 标记：本次更新仅包含文本流式追加
        putBoolean(ChatDetailAdapter.PAYLOAD_TEXT_INLINE_ONLY, true)
      }
    }
    return super.getChangePayload(oldItem, newItem)
  }
}