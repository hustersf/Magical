package com.sofar.wan.android.feature.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.feature.article.ArticleCell
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.feature.banner.BannerCell
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Banners
import com.sofar.wan.android.paging.PageFragment
import com.sofar.wan.android.paging.PageList
import com.sofar.widget.recycler.adapter.CellAdapter
import com.sofar.widget.recycler.adapter.multitype.MultiTypeAdapter

class HomeFragment : PageFragment<Any>() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView.addItemDecoration(ArticleUtil.createItemDecoration())
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
}