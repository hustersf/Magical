package com.sofar.core.ui.recyclerview.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CellViewHolder<T>(
  itemView: View,
  val mCell: Cell<in T> // 使用 in T (逆变) 或 Cell<*> (星投影) 允许承载对应泛型的 Cell
) : RecyclerView.ViewHolder(itemView) {

  private var mBind = false

  init {
    mCell.mViewHolder = this
    mCell.onCreate(itemView)
  }

  fun bind(data: T) {
    mCell.onBind(data)
    mBind = true
  }

  fun unbind() {
    if (mBind) {
      mCell.onUnbind()
    }
    mBind = false
  }
}
