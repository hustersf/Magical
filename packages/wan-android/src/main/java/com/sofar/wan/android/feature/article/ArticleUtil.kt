package com.sofar.wan.android.feature.article

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sofar.wan.android.R
import com.sofar.wan.android.utility.CommonUtil

object ArticleUtil {

  fun createItemDecoration(): RecyclerView.ItemDecoration {
    return object : RecyclerView.ItemDecoration() {
      override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
      ) {
        outRect.left = CommonUtil.dimen(R.dimen.padding_left)
        outRect.right = CommonUtil.dimen(R.dimen.padding_right)
        outRect.top = CommonUtil.dip2px(8f)
        outRect.bottom = CommonUtil.dip2px(8f)
      }
    }
  }

}