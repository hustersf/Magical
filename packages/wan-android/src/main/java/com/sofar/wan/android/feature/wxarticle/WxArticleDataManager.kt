package com.sofar.wan.android.feature.wxarticle

import com.sofar.wan.android.model.Kind

object WxArticleDataManager {

  private val wxArticles = mutableListOf<Kind>()

  fun updateWxArticleTab(list: List<Kind>) {
    wxArticles.clear()
    wxArticles.addAll(list)
  }

  fun getWxArticleByIndex(index: Int): Kind {
    return wxArticles[index]
  }

  fun getWxArticleById(id: Int): Kind {
    wxArticles.forEach {
      if (id == it.id) {
        return it
      }
    }
    return Kind()
  }
}