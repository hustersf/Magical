package com.sofar.wan.android.feature.navi

import com.sofar.wan.android.model.Navigation
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.network.model.Response

class NaviRepository {

  suspend fun getNavigationList(): Response<List<Navigation>> {
    return ApiProvider.get().getNavigationList()
  }
}