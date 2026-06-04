package com.sofar.feature.ai.edge.chat.impl.detail

import android.os.Bundle
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
import io.noties.markwon.Markwon

class ChatDetailAdapter(
  private val diffCallback: ChatDetailDiffCallback = ChatDetailDiffCallback()
) : ListAdapter<MessageEntity, RecyclerView.ViewHolder>(diffCallback) {

  companion object {
    const val PAYLOAD_TEXT_INLINE_ONLY = "payload_text_inline_only"

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

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int,
    payloads: List<Any?>
  ) {
    if (payloads.isNotEmpty()) {
      val bundle = payloads.firstOrNull() as? Bundle

      // 🎯 精准拦截：如果是打字机流式追加，且当前是 AI 文本的 Holder
      if (bundle != null && bundle.getBoolean(PAYLOAD_TEXT_INLINE_ONLY, false)
        && holder is AiTextViewHolder
      ) {
        val item = getItem(position)
        // 直接现场拿最新文本局部刷新，不重绘整个气泡
        holder.updateTextInline(item.textContent)
        return
      }
    }
    // 如果 payloads 为空，或者不是 AI 吐字引起的变更，无缝降级走上面你写好的全量绑定
    super.onBindViewHolder(holder, position, payloads)
  }
}

// ==============================================================================
// ViewHolder 最终分发终点：多模态与角色解耦的同级独立组件设计
// ==============================================================================

/**
 * 用户纯文本气泡
 */
class UserTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val contentTv: TextView = itemView.findViewById(R.id.content_tv)

  fun bind(item: MessageEntity) {
    contentTv.text = item.textContent ?: ""
  }
}

/**
 * AI 纯文本气泡（专门承载打字机高频擦写）
 */
class AiTextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val contentTv: TextView = itemView.findViewById(R.id.content_tv)

  private val markwon = Markwon.create(contentTv.context)

  fun bind(item: MessageEntity) {
    // 首次顶出气泡时，若内容为空自动展示思考兜底字样
    if (item.textContent.isNullOrEmpty()) {
      contentTv.text = itemView.context.getString(R.string.feature_chat_ai_thinking)
    } else {
      updateTextInline(item.textContent)
    }
  }

  fun updateTextInline(newText: String?) {
    markwon.setMarkdown(contentTv, newText ?: "")
  }
}

/**
 * 多模态开发期占位 ViewHolder
 */
class DummyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
