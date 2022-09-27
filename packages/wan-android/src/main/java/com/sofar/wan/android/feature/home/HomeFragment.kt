package com.sofar.wan.android.feature.home

import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.feature.article.ArticleAdapter
import com.sofar.wan.android.feature.article.ArticlePageList
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.paging.PageList
import com.sofar.wan.android.paging.PageFragment
import com.sofar.widget.recycler.adapter.CellAdapter

class HomeFragment : PageFragment<Article>() {

  override fun onCreateAdapter(): CellAdapter<Article> {
    return ArticleAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    return ArticlePageList()
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleUtil.DIFF_CALLBACK
  }

}