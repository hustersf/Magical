package com.sofar.wan.android.feature.article

import com.sofar.wan.android.model.Article
import com.sofar.core.ui.recyclerview.adapter.Cell
import com.sofar.core.ui.recyclerview.adapter.CellAdapter

class ArticleAdapter : CellAdapter<Article>() {

  override fun onCreateCell(viewType: Int): Cell<Article> {
    return ArticleCell()
  }
}