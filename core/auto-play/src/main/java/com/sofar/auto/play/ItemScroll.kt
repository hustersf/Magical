package com.sofar.auto.play

annotation class ItemScroll {
  companion object {
    const val UNKNOWN: Int = 0 //不滚动
    const val TOP: Int = 1 //置顶
    const val ONE_ITEM: Int = 2 //滚动一个item的距离
  }
}
