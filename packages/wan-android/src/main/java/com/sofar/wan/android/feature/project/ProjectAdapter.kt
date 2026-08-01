package com.sofar.wan.android.feature.project

import com.sofar.wan.android.model.Article
import com.sofar.core.ui.recyclerview.adapter.Cell
import com.sofar.core.ui.recyclerview.adapter.CellAdapter

class ProjectAdapter : CellAdapter<Article>() {

  override fun onCreateCell(viewType: Int): Cell<Article> {
    return ProjectCell()
  }
}