package com.sofar.wan.android.feature.wxarticle

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.feature.article.ArticleAdapter
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.paging.PageFragment
import com.sofar.wan.android.paging.PageList
import com.sofar.widget.recycler.adapter.CellAdapter

class WxArticleFragment : PageFragment<Article>() {

  private var index = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    index = arguments?.getInt(ArticleConst.KEY_INDEX, 0) ?: 0
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView.addItemDecoration(ArticleUtil.createItemDecoration())
  }

  override fun onCreateAdapter(): CellAdapter<Article> {
    return ArticleAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    var wxArticle = WxArticleDataManager.getWxArticleByIndex(index)
    return WxArticlePageList(wxArticle)
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleDiffCalculator.getArticleDiffItemCallback()
  }
}