package com.sofar.snapu.feature.daq.model

sealed class DataState<out T> {
  object Loading : DataState<Nothing>()
  data class Success<out T>(val data: T) : DataState<T>()
  data class Error(val exception: Exception) : DataState<Nothing>()
}