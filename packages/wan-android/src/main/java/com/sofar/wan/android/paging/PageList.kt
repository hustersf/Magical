package com.sofar.wan.android.paging

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

abstract class PageList<Key : Any, Value : Any> {

  private var prevKey: Key? = null
  private var nextKey: Key? = null
  private var data = ArrayList<Value>()

  private var loading = false
  private var loadStates = LoadStates.IDLE
  private val _stateFlow = MutableStateFlow<LoadStates?>(null)
  val loadStateFlow: Flow<LoadStates> = _stateFlow.filterNotNull()
  val dataFlow = MutableStateFlow(emptyList<Value>())

  suspend fun load(loadType: LoadType) {
    if (loading) {
      return
    }

    loading = true
    onLoadStart(loadType)
    var params = LoadParams(loadType, prevKey, nextKey)
    var result = doLoad(params)
    loading = false
    when (result) {
      is LoadResult.Page -> {
        onLoadSuccess(loadType, result)
      }
      is LoadResult.Error -> {
        onLoadError(loadType, result.throwable)
      }
    }
  }

  abstract suspend fun doLoad(params: LoadParams<Key>): LoadResult<Key, Value>

  private fun onLoadStart(loadType: LoadType) {
    Log.d("PageList", "onLoadStart loadType=$loadType")
    notifyLoadState(loadType, LoadState.Loading())
  }

  private fun onLoadSuccess(loadType: LoadType, page: LoadResult.Page<Key, Value>) {
    Log.d("PageList", "onLoadSuccess loadType=$loadType")

    if (page.data == null || page.data.isEmpty()) {
      notifyLoadState(loadType, LoadState.NotLoading())
      return
    }

    prevKey = page.prevKey
    nextKey = page.nextKey
    when (loadType) {
      LoadType.REFRESH -> {
        data.clear()
        data.addAll(page.data)
      }
      LoadType.APPEND -> {
        data.addAll(page.data)
      }
      else -> {
        data.addAll(0, page.data)
      }
    }
    dataFlow.value = getData()

    notifyLoadState(loadType, LoadState.NotLoading())
  }

  private fun onLoadError(loadType: LoadType, throwable: Throwable) {
    Log.d("PageList", "onLoadError loadType=$loadType error=$throwable")
    notifyLoadState(loadType, LoadState.Error(throwable))
  }

  private fun notifyLoadState(loadType: LoadType, loadState: LoadState) {
    loadStates = loadStates.modifyState(loadType, loadState)
    _stateFlow.value = loadStates
  }

  private fun getData(): List<Value> {
    val copy = ArrayList<Value>(data.size)
    copy.addAll(data)
    return copy
  }

  fun isEmpty(): Boolean {
    return data.isEmpty()
  }

  fun hasPrev(): Boolean {
    return prevKey != null
  }

  fun hasNext(): Boolean {
    return nextKey != null
  }

}