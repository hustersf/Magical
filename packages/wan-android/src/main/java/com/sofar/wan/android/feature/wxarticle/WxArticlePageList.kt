package com.sofar.wan.android.feature.wxarticle

import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.WxArticle
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.paging.LoadParams
import com.sofar.wan.android.paging.LoadResult
import com.sofar.wan.android.paging.LoadType
import com.sofar.wan.android.paging.PageList

class WxArticlePageList(private val wxArticle: WxArticle) : PageList<Int, Article>() {

  override suspend fun doLoad(params: LoadParams<Int>): LoadResult<Int, Article> {
    return try {
      var pageNo = params.nextKey ?: 1
      if (params.loadType == LoadType.REFRESH) {
        pageNo = 1
      }
      var response = ApiProvider.get().getWxArticles(wxArticle.id, pageNo, 20).data
      //成功需要返回 Page
      var item = response?.items!!
      var nextKey = if (item.isEmpty()) null else pageNo + 1
      LoadResult.Page(item, null, nextKey)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

}