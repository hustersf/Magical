package com.sofar.wan.android.feature.article

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sofar.utility.CollectionUtil
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.webview.WebViewActivity
import com.sofar.widget.recycler.adapter.Cell

class ArticleCell : Cell<Article>() {
  private lateinit var rootView: View

  private lateinit var author: TextView
  private lateinit var chapter: TextView
  private lateinit var time: TextView
  private lateinit var title: TextView

  private lateinit var topTag: TextView
  private lateinit var newTag: TextView
  private lateinit var articleTag: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.article_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    this.rootView = rootView
    author = rootView.findViewById(R.id.author_name)
    chapter = rootView.findViewById(R.id.chapter_name)
    time = rootView.findViewById(R.id.time)
    title = rootView.findViewById(R.id.article_title)

    topTag = rootView.findViewById(R.id.top_tag)
    newTag = rootView.findViewById(R.id.new_tag)
    articleTag = rootView.findViewById(R.id.article_tag)
  }

  override fun onBind(data: Article) {
    super.onBind(data)
    author.text = data.getArticleAuthor()
    chapter.text = data.superChapterName + "/" + data.chapterName
    time.text = data.niceDate
    title.text = Html.fromHtml(data.title)

    if (data.type == 1) {
      topTag.visibility = View.VISIBLE
    } else {
      topTag.visibility = View.GONE
    }
    if (data.fresh) {
      newTag.visibility = View.VISIBLE
    } else {
      newTag.visibility = View.GONE
    }
    if (!CollectionUtil.isEmpty(data.tags)) {
      var sb = StringBuffer()
      data.tags.forEach {
        sb.append(it.name)
        sb.append("·")
      }
      sb.deleteCharAt(sb.lastIndex)
      articleTag.text = sb.toString()
      articleTag.visibility = View.VISIBLE
    } else {
      articleTag.visibility = View.GONE
    }

    rootView.setOnClickListener {
      WebViewActivity.open(rootView.context, data.link)
    }
  }
}