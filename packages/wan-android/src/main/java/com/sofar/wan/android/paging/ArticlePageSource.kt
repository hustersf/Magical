package com.sofar.wan.android.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.network.api.ApiProvider

class ArticlePageSource : PagingSource<Int, Article>() {

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
    return try {
      var pageNo = 0
      if (params is LoadParams.Append) {
        pageNo = params.key
      }
      var response = ApiProvider.get().getArticlePageList(pageNo, params.loadSize).data
      //成功需要返回 Page
      LoadResult.Page(response?.items!!, null, response?.curPage)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
    return null
  }
}