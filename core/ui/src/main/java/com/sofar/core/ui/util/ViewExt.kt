package com.sofar.core.ui.util

import android.graphics.Rect
import android.os.SystemClock
import android.view.View

/**
 * 计算并获取当前 View 在屏幕可见区域内的展示比例。
 *
 * @return 0.0f 代表完全不可见；1.0f 代表 100% 完整展现在屏幕中。返回值范围在 [0.0, 1.0]。
 */
fun View?.getViewShowRatio(): Float {
  // 基础防御：若 View 对象为 null，或者未通过 isShown 进行系统级显隐校验，直接判定不可见
  if (this == null || !this.isShown) {
    return 0f
  }

  val rect = Rect()
  // 利用 Android 官方系统原生矩形裁剪算法，获取实际在屏幕上画出来的物理可见区域
  return if (this.getGlobalVisibleRect(rect)) {
    val visibleArea = rect.height().toLong() * rect.width().toLong()
    val viewArea = this.height.toLong() * this.width.toLong()

    if (viewArea <= 0) return 0f // 过滤分母为 0 的极端测算边界

    // 显式浮点数对齐，精准计算可见比率
    (1.0f * visibleArea / viewArea).coerceIn(0f, 1f)
  } else {
    0f
  }
}

// 默认间隔时间 500ms (完全符合 Material Design 规范)
private const val DEFAULT_INTERVAL = 500L

/**
 * Kotlin 专享：接收 lambda 闭包的高阶函数版防抖点击
 */
fun View.setOnSingleClickListener(
  interval: Long = DEFAULT_INTERVAL,
  action: (View) -> Unit
) {
  this.setOnClickListener(object : View.OnClickListener {
    private var lastClickTime = 0L
    override fun onClick(v: View) {
      val current = SystemClock.elapsedRealtime()
      if (current - lastClickTime >= interval) {
        lastClickTime = current
        action(v)
      }
    }
  })
}

/**
 *混编双向友好：专门提供给 Java 老页面或传统原生 Listener 的重载版本
 * 使用示例 (Java)：ViewExt.setOnSingleClickListener(btn, this);
 */
@JvmOverloads
fun View.setOnSingleClickListener(
  listener: View.OnClickListener,
  interval: Long = DEFAULT_INTERVAL
) {
  this.setOnClickListener(object : View.OnClickListener {
    private var lastClickTime = 0L

    override fun onClick(v: View) {
      val current = SystemClock.elapsedRealtime()
      if (current - lastClickTime >= interval) {
        lastClickTime = current
        listener.onClick(v)
      }
    }
  })
}