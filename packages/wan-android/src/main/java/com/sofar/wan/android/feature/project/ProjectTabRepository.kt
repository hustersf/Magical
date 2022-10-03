package com.sofar.wan.android.feature.project

import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.network.model.Response

class ProjectTabRepository {

  suspend fun getProjectKinds(): Response<List<Kind>> {
    return ApiProvider.get().getProjectKind()
  }

}