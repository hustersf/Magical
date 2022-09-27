package com.sofar.wan.android.paging

data class LoadStates(
  var refresh: LoadState,
  var prepend: LoadState,
  var append: LoadState,
) {

  internal fun modifyState(loadType: LoadType, newState: LoadState): LoadStates {
    return when (loadType) {
      LoadType.APPEND -> copy(
        append = newState
      )
      LoadType.PREPEND -> copy(
        prepend = newState
      )
      LoadType.REFRESH -> copy(
        refresh = newState
      )
    }
  }

  internal companion object {
    val IDLE = LoadStates(
      refresh = LoadState.NotLoading(),
      prepend = LoadState.NotLoading(),
      append = LoadState.NotLoading()
    )
  }
}