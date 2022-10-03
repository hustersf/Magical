package com.sofar.wan.android.feature.wxarticle

import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.network.model.Response

class WxArticleTabRepository {

  suspend fun getWxAuthors(): Response<List<Kind>> {
    return ApiProvider.get().getWxAuthors()
  }

}