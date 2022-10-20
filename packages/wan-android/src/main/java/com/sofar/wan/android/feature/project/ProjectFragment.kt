package com.sofar.wan.android.feature.project

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.feature.article.ArticleDiffCalculator
import com.sofar.wan.android.feature.article.ArticleUtil
import com.sofar.wan.android.feature.base.BasePageFragment
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.paging.PageList
import com.sofar.widget.recycler.adapter.CellAdapter

class ProjectFragment : BasePageFragment<Article>() {

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
    return ProjectAdapter()
  }

  override fun onCreatePageList(): PageList<*, Article> {
    var project = ProjectDataManager.getProjectIndex(index)
    return ProjectPageList(project)
  }

  override fun onCreateDiffCallback(): DiffUtil.ItemCallback<Article> {
    return ArticleDiffCalculator.getArticleDiffItemCallback()
  }
}