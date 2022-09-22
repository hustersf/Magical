package com.sofar.wan.android.feature.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Article

class HomeAdapter : PagingDataAdapter<Article, HomeAdapter.ItemViewHolder>(DIFF) {

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.article_item, parent, false)
    return ItemViewHolder(view)
  }

  class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val name: TextView = itemView.findViewById(R.id.name)

    fun bind(article: Article?) {
      name.text = article?.title
    }
  }

  companion object {
    val DIFF = object : DiffUtil.ItemCallback<Article>() {
      override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
      }

      override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
      }
    }
  }

}