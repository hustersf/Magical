package com.sofar.wan.android.feature.category

import com.sofar.wan.android.model.Article
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.paging.LoadParams
import com.sofar.wan.android.paging.LoadResult
import com.sofar.wan.android.paging.LoadType
import com.sofar.wan.android.paging.PageList

class CategoryPageList : PageList<Int, Article>() {
  private var cid = 0

  fun updateCid(id: Int) {
    cid = id
  }

  override suspend fun doLoad(params: LoadParams<Int>): LoadResult<Int, Article> {
    return try {
      var pageStart = 0
      var pageNo = params.nextKey ?: pageStart
      if (params.loadType == LoadType.REFRESH) {
        pageNo = pageStart
      }
      var response = ApiProvider.get().getCategoryArticles(pageNo, cid).data
      //成功需要返回 Page
      var item = response?.items!!
      var nextKey = if (item.isEmpty()) null else pageNo + 1
      LoadResult.Page(item, null, nextKey)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }
}