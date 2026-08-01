package com.sofar.core.ui.recyclerview

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 保证子View的中线始终与RecyclerView本身的中线对齐
 *
 * 与PagerSnapHelper的区别在于，不算decoration and margin，保证视觉效果上子View始终在RecyclerView的中间
 */
class CenterPagerSnapHelper : PagerSnapHelper() {

  private var mVerticalHelper: OrientationHelper? = null
  private var mHorizontalHelper: OrientationHelper? = null

  override fun calculateDistanceToFinalSnap(
    layoutManager: RecyclerView.LayoutManager,
    targetView: View
  ): IntArray {
    val out = IntArray(2)
    if (layoutManager.canScrollHorizontally()) {
      out[0] = distanceToCenter(
        layoutManager, targetView,
        getHorizontalHelper(layoutManager)
      )
    } else {
      out[0] = 0
    }

    if (layoutManager.canScrollVertically()) {
      out[1] = distanceToCenter(
        layoutManager, targetView,
        getVerticalHelper(layoutManager)
      )
    } else {
      out[1] = 0
    }
    return out
  }

  private fun distanceToCenter(
    layoutManager: RecyclerView.LayoutManager,
    targetView: View,
    helper: OrientationHelper
  ): Int {
    val childCenter = targetView.left + targetView.measuredWidth / 2
    val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
    return childCenter - containerCenter
  }

  private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    return mVerticalHelper ?: OrientationHelper.createVerticalHelper(layoutManager).also {
      mVerticalHelper = it
    }
  }

  private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    return mHorizontalHelper ?: OrientationHelper.createHorizontalHelper(layoutManager).also {
      mHorizontalHelper = it
    }
  }
}
