package com.sofar.wan.android.feature.category

import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.network.model.Response

class CategoryRepository {

  suspend fun getCategoryList(): Response<List<Kind>> {
    return ApiProvider.get().getCategoryList()
  }
}