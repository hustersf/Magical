package com.sofar.wan.android.feature.article

import androidx.recyclerview.widget.DiffUtil
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Banner

object ArticleDiffCalculator {

  fun getCommonDiffItemCallback() = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      if (oldItem is Article && newItem is Article) {
        return oldItem.id == newItem.id
      } else if (oldItem is Banner && newItem is Banner) {
        return oldItem.id == newItem.id
      }
      return false
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      if (oldItem is Article && newItem is Article) {
        return oldItem.title == newItem.title
      } else if (oldItem is Banner && newItem is Banner) {
        return oldItem.title == newItem.title
      }
      return false
    }
  }

  fun getArticleDiffItemCallback() = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
      return oldItem.title == newItem.title
    }
  }
}