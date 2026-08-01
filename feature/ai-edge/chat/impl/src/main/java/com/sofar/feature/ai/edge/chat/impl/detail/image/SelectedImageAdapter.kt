package com.sofar.feature.ai.edge.chat.impl.detail.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.image.clearImage
import com.sofar.image.loadImage
import com.sofar.feature.ai.edge.chat.impl.R

internal class SelectedImageAdapter(
  private val onAddClick: ((View) -> Unit)? = null,
  private val onDeleteClick: ((SelectedImageState) -> Unit)? = null
) : ListAdapter<SelectedImageState, ImageViewHolder>(ImageDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.feature_chat_detail_selected_image_item, parent, false)
    return ImageViewHolder(view)
  }

  override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
    val itemState = getItem(position)

    if (itemState.isAddButton) {
      holder.imageView.clearImage()
      holder.imageView.setImageDrawable(null)
      holder.imageMaskIv.visibility = View.GONE
      holder.addIv.visibility = View.VISIBLE
      holder.deleteIv.visibility = View.GONE
      holder.itemView.setOnClickListener { view ->
        onAddClick?.invoke(view)
      }
    } else {
      itemState.path?.let { holder.imageView.loadImage(it) }
      val isEditable = onDeleteClick != null
      holder.addIv.visibility = View.GONE
      holder.deleteIv.visibility = if (isEditable) View.VISIBLE else View.GONE
      holder.imageMaskIv.visibility = View.VISIBLE

      if (isEditable) {
        holder.itemView.setOnClickListener(null)
        holder.deleteIv.setOnClickListener { onDeleteClick.invoke(itemState) }
      } else {
        holder.deleteIv.setOnClickListener(null)
        holder.itemView.setOnClickListener {
          val path = itemState.path
          if (!path.isNullOrEmpty()) {
            ImagePreviewActivity.launch(holder.itemView.context, path)
          }
        }
      }
    }
  }
}

class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val imageView: ImageView = view.findViewById(R.id.preview_iv)
  val imageMaskIv: ImageView = view.findViewById(R.id.image_mask_iv)
  val addIv: ImageView = view.findViewById(R.id.add_iv)
  val deleteIv: ImageView = view.findViewById(R.id.delete_iv)
}

class ImageDiffCallback : DiffUtil.ItemCallback<SelectedImageState>() {

  override fun areItemsTheSame(
    oldItem: SelectedImageState,
    newItem: SelectedImageState
  ): Boolean {
    return if (oldItem.isAddButton && newItem.isAddButton) true
    else oldItem.path.toString() == newItem.path.toString()
  }

  override fun areContentsTheSame(
    oldItem: SelectedImageState,
    newItem: SelectedImageState
  ): Boolean {
    return oldItem == newItem
  }
}
