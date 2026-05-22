package com.sofar.core.ui.state

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val exception: Throwable) : UiState<Nothing>
}