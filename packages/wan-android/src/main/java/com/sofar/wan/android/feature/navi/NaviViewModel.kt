package com.sofar.wan.android.feature.navi

import androidx.lifecycle.ViewModel
import com.sofar.wan.android.model.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NaviViewModel : ViewModel() {
  private val repository = NaviRepository()

  val naviDataFlow = flow {
    val data = mutableListOf<Navigation>()
    data.addAll(repository.getNavigationList().data ?: emptyList())
    emit(data)
  }.flowOn(Dispatchers.Default)

  override fun onCleared() {
    super.onCleared()
  }

}