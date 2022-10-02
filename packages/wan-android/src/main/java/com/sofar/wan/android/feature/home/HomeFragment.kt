package com.sofar.wan.android.feature.home

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sofar.wan.android.R
import com.sofar.wan.android.feature.article.ArticleCell
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.banner.BannerCell
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Banners
import com.sofar.wan.android.paging.PageFragment
import com.sofar.wan.android.paging.PageList
import com.sofar.wan.android.utility.CommonUtil
import com.sofar.widget.recycler.adapter.CellAdapter
import com.sofar.widget.recycler.adapter.multitype.MultiTypeAdapter

class HomeFragment : PageFragment<Any>() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView.addItemDecoration(createItemDecoration())
  }

  override fun onCreateAdapter(): CellAdapter<Any> {
    var adapter = MultiTypeAdapter()
    adapter.register(Article::class.java) { ArticleCell() }
    adapter.register(Banners::class.java) { BannerCell() }
    return adapter
  }

  override fun onCreatePageList(): PageList<*, Any> {
    return HomePageList()
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Any> {
    return ArticleDiffCalculator.getCommonDiffItemCallback()
  }

  private fun createItemDecoration(): RecyclerView.ItemDecoration {
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