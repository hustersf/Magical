package com.sofar.wan.android.feature.article

import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.model.Article

object ArticleUtil {

  val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem.title == newItem.title
    }
  }
}