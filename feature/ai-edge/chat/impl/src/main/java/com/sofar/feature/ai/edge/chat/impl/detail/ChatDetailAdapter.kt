package com.sofar.feature.ai.edge.chat.impl.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageRole
import com.sofar.core.ai.edge.data.entity.chat.ChatMessageType
import com.sofar.core.ai.edge.database.entity.MessageEntity
import com.sofar.core.ui.image.RoundImageView
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import com.sofar.feature.ai.edge.chat.impl.R
import com.sofar.feature.ai.edge.chat.impl.detail.image.ImagePreviewActivity
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageAdapter
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageState
import com.sofar.image.loadImage
import io.noties.markwon.Markwon
import com.sofar.core.ui.R as coreUiR
import com.sofar.feature.ai.edge.chat.api.R as chatR

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

      TYPE_USER_IMAGE -> UserImageViewHolder(
        inflater.inflate(
          R.layout.feature_chat_detail_user_image_item,
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
      is UserImageViewHolder -> holder.bind(item)
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
      contentTv.text = itemView.context.getString(chatR.string.feature_chat_ai_thinking)
    } else {
      updateTextInline(item.textContent)
    }
  }

  fun updateTextInline(newText: String?) {
    markwon.setMarkdown(contentTv, newText ?: "")
  }
}

/**
 * 用户带图片的气泡
 */
class UserImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val contentTv: TextView = itemView.findViewById(R.id.content_tv)
  private val textCardView: View = itemView.findViewById(R.id.text_card_view)
  private val singleIv: RoundImageView = itemView.findViewById(R.id.single_preview_iv)
  private val singleMaskIv: RoundImageView = itemView.findViewById(R.id.single_image_mask_iv)
  private val multiImageRecyclerView: RecyclerView = itemView.findViewById(R.id.multi_image_rv)

  private val subImageAdapter = SelectedImageAdapter()

  init {
    multiImageRecyclerView.layoutManager =
      LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
    multiImageRecyclerView.adapter = subImageAdapter
    val imagePadding = itemView.context.resources.getDimension(coreUiR.dimen.core_ui_spacing_sm)
      .toInt()
    multiImageRecyclerView.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.HORIZONTAL,
        imagePadding,
        imagePadding
      )
    )
  }

  fun bind(item: MessageEntity) {
    val paths = item.filePath ?: emptyList()

    // 动态判断并显示单图、多图或隐藏
    if (paths.size == 1) {
      singleIv.visibility = View.VISIBLE
      singleMaskIv.visibility = View.VISIBLE
      multiImageRecyclerView.visibility = View.GONE
      singleIv.loadImage(paths.first())
      singleIv.setOnClickListener {
        ImagePreviewActivity.launch(itemView.context, paths.first())
      }
    } else if (paths.size > 1) {
      singleIv.visibility = View.GONE
      singleMaskIv.visibility = View.GONE
      multiImageRecyclerView.visibility = View.VISIBLE
      subImageAdapter.submitList(paths.map {
        SelectedImageState(
          path = it,
          isAddButton = false
        )
      })
    } else {
      singleIv.visibility = View.GONE
      multiImageRecyclerView.visibility = View.GONE
    }

    // 完美处理纯图或图文：如果没发字（纯图发送），直接隐藏整个卡片容器，气泡完美消失，只留裸露的原图！
    if (item.textContent.isNullOrEmpty()) {
      textCardView.visibility = View.GONE
    } else {
      textCardView.visibility = View.VISIBLE
      contentTv.text = item.textContent
    }
  }
}

/**
 * 多模态开发期占位 ViewHolder
 */
class DummyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
