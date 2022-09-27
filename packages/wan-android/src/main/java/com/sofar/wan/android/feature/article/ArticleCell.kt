package com.sofar.wan.android.feature.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Article
import com.sofar.widget.recycler.adapter.Cell

class ArticleCell : Cell<Article>() {

  private lateinit var name: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.article_item, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    name = rootView.findViewById(R.id.name)
  }

  override fun onBind(data: Article) {
    super.onBind(data)
    name.text = data.title
  }
}