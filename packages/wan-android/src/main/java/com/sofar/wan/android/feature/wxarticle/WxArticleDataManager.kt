package com.sofar.wan.android.feature.wxarticle

import com.sofar.wan.android.model.WxArticle

object WxArticleDataManager {

  private val wxArticles = mutableListOf<WxArticle>()

  fun updateWxArticleTab(list: List<WxArticle>) {
    wxArticles.clear()
    wxArticles.addAll(list)
  }

  fun getWxArticleByIndex(index: Int): WxArticle {
    return wxArticles[index]
  }

  fun getWxArticleById(id: Int): WxArticle {
    wxArticles.forEach {
      if (id == it.id) {
        return it
      }
    }
    return WxArticle()
  }
}