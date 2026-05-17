package com.sofar.core.ui.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 处理 [RecyclerView] 线性布局（LinearLayoutManager）的分割，控制分割的大小。
 */
class LinearMarginItemDecoration(
  private val orientation: Int,
  private val firstSpace: Int,
  private val lastSpace: Int,
  private val betweenSpace: Int
) : RecyclerView.ItemDecoration() {

  /**
   * 构造函数：仅设置间距。
   *
   * @param orientation  方向 [RecyclerView.HORIZONTAL] 或者 [RecyclerView.VERTICAL]
   * @param betweenSpace 每个 Item 之间的距离
   */
  constructor(orientation: Int, betweenSpace: Int) : this(orientation, 0, 0, betweenSpace)

  /**
   * 构造函数：设置两端距离与中间间距。
   *
   * @param orientation  方向
   * @param sideSpace    离两端（上下或左右）的距离
   * @param betweenSpace 每个 Item 之间的距离
   */
  constructor(orientation: Int, sideSpace: Int, betweenSpace: Int) : this(
    orientation,
    sideSpace,
    sideSpace,
    betweenSpace
  )

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    val totalCount = parent.adapter?.itemCount ?: 0
    val childPosition = parent.getChildAdapterPosition(view)

    if (childPosition == RecyclerView.NO_POSITION) return

    if (orientation == RecyclerView.HORIZONTAL) {
      outRect.left = if (childPosition == 0) firstSpace else betweenSpace
      outRect.right = if (childPosition == totalCount - 1) lastSpace else 0
    } else {
      outRect.top = if (childPosition == 0) firstSpace else betweenSpace
      outRect.bottom = if (childPosition == totalCount - 1) lastSpace else 0
    }
  }
}
