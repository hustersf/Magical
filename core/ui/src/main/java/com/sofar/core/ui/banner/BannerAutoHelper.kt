package com.sofar.core.ui.banner

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * ViewPager2 自动轮播与指示器联动辅助器。
 *
 * 自动接管轮播图的定时滚动生命周期。当视图滑出屏幕时自动暂停，滑回屏幕时自动恢复。
 * 内部全权处理无限循环滚动的页码与圆点指示器（BannerIndicator）的余数映射对接。
 */
class BannerAutoHelper(
  private val viewPager2: ViewPager2,
  private val indicator: BannerIndicator
) {

  private var scrollInterval = 3000L
  private var itemCount = 0
  private var scrollState = ViewPager2.SCROLL_STATE_IDLE

  private val handler = Handler(Looper.getMainLooper())

  private val scrollRunnable = Runnable {
    val current = viewPager2.currentItem
    viewPager2.setCurrentItem(current + 1, true)
  }

  // 生命周期全自动无痕联动。滑出屏幕自动 stop，滑进屏幕自动 start，严防后台轮播引起的电量隐性消耗
  private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View) {
      start()
    }

    override fun onViewDetachedFromWindow(v: View) {
      stop()
    }
  }

  private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
    override fun onPageScrollStateChanged(state: Int) {
      scrollState = state
      updateScrollState()
    }
  }

  init {
    viewPager2.addOnAttachStateChangeListener(attachStateChangeListener)
    viewPager2.registerOnPageChangeCallback(onPageChangeCallback)
  }

  fun setCount(count: Int) {
    this.itemCount = count
  }

  fun setScrollInterval(interval: Long) {
    this.scrollInterval = interval
  }

  fun start() {
    updateScrollState()
  }

  fun stop() {
    handler.removeCallbacks(scrollRunnable)
  }

  /**
   * 核心动作中枢：动态对滑动手势、页码和定时任务进行仲裁
   */
  private fun updateScrollState() {
    if (scrollState != ViewPager2.SCROLL_STATE_IDLE || itemCount <= 1) {
      // 用户正在拖拽滑动中，或者是单页 Banner，立刻掐断下一发定时器的排队
      handler.removeCallbacks(scrollRunnable)
    } else {
      // 当页面重回静止（IDLE），先清空队列，重新派发下一轮的延时轮播任务
      handler.removeCallbacks(scrollRunnable)
      handler.postDelayed(scrollRunnable, scrollInterval)
    }

    // 🎯 无限循环映射：只有当手指松开页面静止时，才去触发指示器的无感高亮更新
    if (scrollState == RecyclerView.SCROLL_STATE_IDLE && itemCount > 1) {
      val position = viewPager2.currentItem
      indicator.setIndicator(position % itemCount)
    }
  }

  /**
   * 外部销毁入口。
   * 当所在的 Fragment 或 Activity 被摧毁时，必须调用此方法彻底卸载解绑。
   */
  fun destroy() {
    stop()
    viewPager2.removeOnAttachStateChangeListener(attachStateChangeListener)
    viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback)
  }
}
