package com.sofar.wan.android.feature.course

import androidx.lifecycle.ViewModel
import com.sofar.wan.android.model.Kind
import kotlinx.coroutines.flow.flow

class CourseViewModel : ViewModel() {
  private val repository = CourseRepository()

  val courseDataFlow = flow {
    val data = mutableListOf<Kind>()
    data.addAll(repository.getCourseList().data ?: emptyList())
    emit(data)
  }
}