package com.sofar.wan.android.feature.home

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.feature.article.ArticlePageSource
import kotlinx.coroutines.flow.Flow

class HomeRepository {

  //对外暴露 Flow<PagingData<ITEM>>
  fun getPagingData(): Flow<PagingData<Article>> {
    return Pager(config = PagingConfig(pageSize = 20),
      pagingSourceFactory = { ArticlePageSource() }).flow
  }
}