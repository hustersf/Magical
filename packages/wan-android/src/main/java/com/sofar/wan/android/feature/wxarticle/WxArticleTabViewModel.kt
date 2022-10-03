package com.sofar.wan.android.feature.wxarticle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.flow

class WxArticleTabViewModel : ViewModel() {
  private val repository = WxArticleTabRepository()

  val wxAuthorDataFlow = flow {
    val data = repository.getWxAuthors().data ?: emptyList()
    emit(data)
  }
}