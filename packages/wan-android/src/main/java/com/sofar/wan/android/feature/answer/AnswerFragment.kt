package com.sofar.wan.android.feature.answer

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.feature.article.ArticleAdapter
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.paging.PageFragment
import com.sofar.wan.android.paging.PageList
import com.sofar.widget.recycler.adapter.CellAdapter

class AnswerFragment : PageFragment<Article>() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView.addItemDecoration(ArticleUtil.createItemDecoration())
  }

  override fun onCreateAdapter(): CellAdapter<Article> {
    return ArticleAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    return AnswerPageList()
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleDiffCalculator.getArticleDiffItemCallback()
  }
}