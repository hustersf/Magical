package com.sofar.feature.ai.edge.chat.impl.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageRole
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageType
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.feature.ai.edge.chat.impl.R

class ChatDetailAdapter(
  private val diffCallback: ChatDetailDiffCallback = ChatDetailDiffCallback()
) : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(diffCallback) {

  companion object {
    const val TYPE_USER_TEXT = 1
    const val TYPE_AI_TEXT = 2
    const val TYPE_USER_IMAGE = 3
    const val TYPE_AI_IMAGE = 4
    const val TYPE_USER_AUDIO = 5
    const val TYPE_AI_AUDIO = 6
  }

  override fun getItemViewType(position: Int): Int {
    val message = getItem(position)
    return if (message.role == ChatMessageRole.USER) {
      when (message.contentType) {
        ChatMessageType.IMAGE -> TYPE_USER_IMAGE
        ChatMessageType.AUDIO -> TYPE_USER_AUDIO
        else -> TYPE_USER_TEXT
      }
    } else {
      when (message.contentType) {
        ChatMessageType.IMAGE -> TYPE_AI_IMAGE
        ChatMessageType.AUDIO -> TYPE_AI_AUDIO
        else -> TYPE_AI_TEXT
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      TYPE_USER_TEXT -> UserTextViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_user_text_item,
          parent,
          false
        )
      )

      TYPE_AI_TEXT -> AiTextViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_ai_text_item,
          parent,
          false
        )
      )

      // 临时占位，未来直接在这里替换为你写好的外部独立多模态 ViewHolder
      TYPE_USER_IMAGE -> DummyViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_user_text_item,
          parent,
          false
        )
      )

      TYPE_AI_IMAGE -> DummyViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_ai_text_item,
          parent,
          false
        )
      )

      TYPE_USER_AUDIO -> DummyViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_user_text_item,
          parent,
          false
        )
      )

      TYPE_AI_AUDIO -> DummyViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_ai_text_item,
          parent,
          false
        )
      )

      else -> throw IllegalArgumentException("未知的 viewType: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = getItem(position)
    when (holder) {
      is UserTextViewHolder -> holder.bind(item)
      is AiTextViewHolder -> holder.bind(item)
    }
  }

  /**
   * 💎 大模型局部非阻塞流式刷新接口
   */
  fun updateLastAiTextMessageInline(recyclerView: RecyclerView, accumulatedText: String) {
    if (itemCount == 0) return
    val lastIndex = itemCount - 1
    val lastMessage = getItem(lastIndex)

    if (lastMessage.role == ChatMessageRole.ASSISTANT && lastMessage.contentType == ChatMessageType.TEXT) {
      val holder = recyclerView.findViewHolderForAdapterPosition(lastIndex) as? AiTextViewHolder
      holder?.updateTextInline(accumulatedText)
    }
  }
}

// ==============================================================================
// ViewHolder 最终分发终点：多模态与角色解耦的同级独立组件设计
// ==============================================================================

/**
 * 用户纯文本气泡
 */
class UserTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val tvContent: TextView = itemView.findViewById(R.id.content_tv)

  fun bind(item: MessageEntity) {
    tvContent.text = item.textContent ?: ""
  }
}

/**
 * AI 纯文本气泡（专门承载打字机高频擦写）
 */
class AiTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val tvContent: TextView = itemView.findViewById(R.id.content_tv)

  fun bind(item: MessageEntity) {
    // 首次顶出气泡时，若内容为 null 自动展示思考兜底字样
    tvContent.text = item.textContent ?: "AI 正在思考..."
  }

  fun updateTextInline(newText: String) {
    tvContent.text = newText
  }
}

/**
 * 多模态开发期占位 ViewHolder
 */
class DummyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
