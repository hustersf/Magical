package com.sofar.wan.android.feature.home

import com.sofar.wan.android.model.Banners
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.paging.LoadParams
import com.sofar.wan.android.paging.LoadResult
import com.sofar.wan.android.paging.LoadType
import com.sofar.wan.android.paging.PageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class HomePageList : PageList<Int, Any>() {

  override suspend fun doLoad(params: LoadParams<Int>): LoadResult<Int, Any> {
    return try {
      var pageNo = 0
      if (params.nextKey != null) {
        pageNo = params.nextKey
      }
      if (params.loadType == LoadType.REFRESH) {
        pageNo = 0
      }

      var item = mutableListOf<Any>()
      var nextKey: Int? = null
      withContext(Dispatchers.Default) {
        if (pageNo == 0) {
          val topsDeferred = async {
            return@async ApiProvider.get().getArticleTopList().data
          }
          val bannersDeferred = async {
            return@async ApiProvider.get().getBanner().data
          }

          val articlesDeferred = async {
            return@async ApiProvider.get().getArticlePageList(pageNo, 20).data
          }

          val banners = bannersDeferred.await() ?: emptyList()
          item.add(Banners(banners))

          val tops = topsDeferred.await() ?: emptyList()
          item.addAll(tops)

          val articles = articlesDeferred.await()?.items ?: emptyList()
          item.addAll(articles)
          nextKey = articlesDeferred.await()?.curPage
        } else {
          val response = ApiProvider.get().getArticlePageList(pageNo, 20).data
          item.addAll(response?.items ?: emptyList())
          nextKey = response?.curPage
        }
      }
      //成功需要返回 Page
      LoadResult.Page(item, null, nextKey)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

}