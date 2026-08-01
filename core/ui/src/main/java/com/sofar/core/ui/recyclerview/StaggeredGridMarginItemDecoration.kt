package com.sofar.core.ui.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 处理 StaggeredGridLayoutManager 的分割，控制分割的大小.
 */
class StaggeredGridMarginItemDecoration @JvmOverloads constructor(
  private val mSpanCount: Int,
  private val mItemSpace: Int,
  private val mTopBottomSpace: Int = mItemSpace,
  private val mLeftRightSpace: Int = mItemSpace
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    val totalCount = parent.adapter?.itemCount ?: 0
    val childPosition = parent.getChildAdapterPosition(view)

    // 安全类型转型，防止因 LayoutParams 类型不匹配导致崩溃
    val layoutParams = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams ?: return
    val spanIndex = layoutParams.spanIndex

    outRect.left = mItemSpace / 2
    outRect.right = mItemSpace / 2
    outRect.top = mItemSpace
    outRect.bottom = 0

    // 左右边距计算
    when (spanIndex) {
      0 -> outRect.left = mLeftRightSpace
      mSpanCount - 1 -> outRect.right = mLeftRightSpace
    }

    // 上下边距行数计算
    val isFirstLine = childPosition < mSpanCount
    val maxLineIndex = (totalCount - 1) / mSpanCount
    val currentLineIndex = childPosition / mSpanCount
    val isLastLine = maxLineIndex == currentLineIndex

    when {
      isFirstLine -> outRect.top = mTopBottomSpace
      isLastLine -> outRect.bottom = mTopBottomSpace
    }
  }
}
