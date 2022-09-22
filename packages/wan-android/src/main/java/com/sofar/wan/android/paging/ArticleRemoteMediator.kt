package com.sofar.wan.android.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.sofar.wan.android.model.Article

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator : RemoteMediator<Int, Article>() {
  override suspend fun load(loadType: LoadType, state: PagingState<Int, Article>): MediatorResult {
    TODO("Not yet implemented")
  }
}