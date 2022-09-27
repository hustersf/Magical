package com.sofar.wan.android.paging

sealed class LoadState {

  /**
   * 数据加载成功
   */
  class NotLoading : LoadState() {
    override fun equals(other: Any?): Boolean {
      return super.equals(other)
    }
  }

  /**
   * 数据加载中...
   */
  class Loading : LoadState() {
    override fun equals(other: Any?): Boolean {
      return super.equals(other)
    }
  }

  /**
   * 数据加载错误
   */
  class Error(val error: Throwable) : LoadState()
}