package com.sofar.base.util

import android.os.SystemClock
import android.view.View
import java.util.concurrent.atomic.AtomicLong

object FastClickUtil {

  // 默认间隔时间 500ms (符合 Material Design 规范)
  const val DEFAULT_INTERVAL = 500L

  // 存储最后点击时间戳（线程安全）
  private val lastClickTime = AtomicLong(0)

  /**
   * 判断是否快速重复点击
   * @param interval 间隔时间（毫秒）
   */
  @Synchronized
  fun isFastClick(interval: Long = DEFAULT_INTERVAL): Boolean {
    val current = SystemClock.elapsedRealtime()
    if (current - lastClickTime.get() >= interval) {
      lastClickTime.set(current)
      return false
    }
    return true
  }
}

// 为 View 添加扩展函数（Kotlin）
fun View.setOnSingleClickListener(
  interval: Long = FastClickUtil.DEFAULT_INTERVAL,
  action: (View) -> Unit
) {
  setOnClickListener {
    if (!FastClickUtil.isFastClick(interval)) {
      action(it)
    }
  }
}

fun View.setOnSingleClickListener(listener: View.OnClickListener) {
  setOnClickListener {
    if (!FastClickUtil.isFastClick()) {
      listener.onClick(it)
    }
  }
}