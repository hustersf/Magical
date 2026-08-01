package com.sofar.core.ui.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 处理 GridLayoutManager 的分割，控制分割的大小.
 */
class GridMarginItemDecoration @JvmOverloads constructor(
  private val mOrientation: Int,
  private val mSpanCount: Int,
  private val mItemSpace: Int,
  private val mLeftSpace: Int = 0,
  private val mRightSpace: Int = 0
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    val position = parent.getChildAdapterPosition(view)
    val itemCount = parent.adapter?.itemCount ?: 0

    if (mOrientation == RecyclerView.VERTICAL) {
      val column = position % mSpanCount
      outRect.top = 0
      outRect.bottom = mItemSpace

      when (column) {
        0 -> {
          outRect.left = mLeftSpace
          outRect.right = mItemSpace / 2
        }

        mSpanCount - 1 -> {
          outRect.left = mItemSpace / 2
          outRect.right = mRightSpace
        }

        else -> {
          outRect.left = mItemSpace / 2
          outRect.right = mItemSpace / 2
        }
      }
    } else {
      val row = position % mSpanCount
      val column = position / mSpanCount
      outRect.bottom = mItemSpace
      if (row == mSpanCount - 1) {
        // 实际上不生效，bottom的值虽然是0，但视觉效果还是有mItemSpace的间距
        outRect.bottom = 0
      }

      outRect.left = if (column == 0) mLeftSpace else mItemSpace / 2
      outRect.right = if (column == itemCount / mSpanCount - 1) mRightSpace else mItemSpace / 2
    }
  }
}
