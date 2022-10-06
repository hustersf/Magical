package com.sofar.wan.android.feature.course

import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.network.api.ApiProvider
import com.sofar.wan.android.network.model.Response

class CourseRepository {

  suspend fun getCourseList(): Response<List<Kind>> {
    return ApiProvider.get().getCourseList()
  }
}