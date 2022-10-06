package com.sofar.wan.android.feature.category

import androidx.lifecycle.ViewModel
import com.sofar.wan.android.model.Kind
import kotlinx.coroutines.flow.flow

class CategoryViewModel : ViewModel() {

  private val repository = CategoryRepository()

  val categoryDataFlow = flow {
    val data = mutableListOf<Kind>()
    data.addAll(repository.getCategoryList().data ?: emptyList())
    emit(data)
  }

  override fun onCleared() {
    super.onCleared()
  }
}