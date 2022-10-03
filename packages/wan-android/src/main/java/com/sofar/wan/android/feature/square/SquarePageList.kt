package com.sofar.wan.android.feature.square

import com.sofar.wan.android.model.Article
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.paging.LoadParams
import com.sofar.wan.android.paging.LoadResult
import com.sofar.wan.android.paging.LoadType
import com.sofar.wan.android.paging.PageList

class SquarePageList : PageList<Int, Article>() {

  override suspend fun doLoad(params: LoadParams<Int>): LoadResult<Int, Article> {
    return try {
      var pageNo = params.nextKey ?: 1
      if (params.loadType == LoadType.REFRESH) {
        pageNo = 0
      }
      var response = ApiProvider.get().getSquarePageList(pageNo, 20).data
      //成功需要返回 Page
      var item = response?.items!!
      LoadResult.Page(item, null, response?.curPage)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

}