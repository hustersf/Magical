package com.sofar.wan.android.feature.project

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.webview.WebViewActivity
import com.sofar.widget.recycler.adapter.Cell

class ProjectCell : Cell<Article>() {
  private lateinit var rootView: View

  private lateinit var cover: ImageView
  private lateinit var title: TextView
  private lateinit var subTitle: TextView
  private lateinit var author: TextView
  private lateinit var time: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.project_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    this.rootView = rootView
    cover = rootView.findViewById(R.id.project_cover)
    title = rootView.findViewById(R.id.project_title)
    subTitle = rootView.findViewById(R.id.project_sub_title)
    author = rootView.findViewById(R.id.author_name)
    time = rootView.findViewById(R.id.time)
  }

  override fun onBind(data: Article) {
    super.onBind(data)
    Glide.with(cover).load(data.coverUrl).into(cover)
    title.text = Html.fromHtml(data.title)
    subTitle.text = Html.fromHtml(data.desc)
    author.text = data.getArticleAuthor()
    time.text = data.niceDate

    rootView.setOnClickListener {
      WebViewActivity.open(rootView.context, data.link)
    }
  }
}