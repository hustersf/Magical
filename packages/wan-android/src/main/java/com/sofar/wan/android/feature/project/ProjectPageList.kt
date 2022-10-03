package com.sofar.wan.android.feature.project

import com.sofar.wan.android.feature.article.ArticleConst
import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.paging.LoadParams
import com.sofar.wan.android.paging.LoadResult
import com.sofar.wan.android.paging.LoadType
import com.sofar.wan.android.paging.PageList

class ProjectPageList(private val project: Kind) : PageList<Int, Article>() {

  override suspend fun doLoad(params: LoadParams<Int>): LoadResult<Int, Article> {
    return try {
      var pageStart = if (project.id == ArticleConst.NEW_PROJECT_CID) 0 else 1
      var pageNo = params.nextKey ?: pageStart
      if (params.loadType == LoadType.REFRESH) {
        pageNo = pageStart
      }
      var response = if (project.id == ArticleConst.NEW_PROJECT_CID) {
        ApiProvider.get().getNewProjectArticles(pageNo, 20).data
      } else {
        ApiProvider.get().getProjectKindArticles(project.id, pageNo, 20).data
      }
      //成功需要返回 Page
      var item = response?.items!!
      var nextKey = if (item.isEmpty()) null else pageNo + 1
      LoadResult.Page(item, null, nextKey)
    } catch (e: Exception) {
      LoadResult.Error(e)
    }
  }

}