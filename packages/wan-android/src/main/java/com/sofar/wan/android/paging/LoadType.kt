package com.sofar.wan.android.paging

enum class LoadType {
  /**
   * 刷新数据
   */
  REFRESH,

  /**
   * 向前加载数据
   */
  PREPEND,

  /**
   * 向后加载数据
   */
  APPEND
}