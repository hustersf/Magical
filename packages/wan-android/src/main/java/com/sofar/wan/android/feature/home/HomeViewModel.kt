package com.sofar.wan.android.feature.home

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.sofar.wan.android.model.Article
import kotlinx.coroutines.flow.Flow

class HomeViewModel : ViewModel() {

  var repository: HomeRepository = HomeRepository()

  // 向 UI 层暴露 Flow<PagingData<ITEM>
  val pageFlow: Flow<PagingData<Article>> = repository.getPagingData()
}