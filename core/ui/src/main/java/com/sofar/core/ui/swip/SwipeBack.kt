package com.sofar.core.ui.swip

import android.app.Activity
import android.graphics.Color
import android.view.ViewGroup

/**
 * 支持滑动返回的Activity需要设置透明背景主题
 * <item name="android:windowIsTranslucent">true</item>
 * <item name="android:windowBackground">@android:color/transparent</item>
 */
class SwipeBack {

  companion object {
    /**
     * 关联滑动返回
     * 通过给 func 设置默认值 null，一个方法即可同时兼容 Java 的两个 attach 重载调用
     */
    @JvmStatic
    @JvmOverloads
    fun attach(activity: Activity, func: SwipeLayout.OnSwipedListener? = null): SwipeLayout {
      val layout = SwipeLayout(activity)
      Helper(layout).attachSwipeBack(activity, func)
      return layout
    }
  }

  internal class Helper(private val mSwipeLayout: SwipeLayout) {

    fun attachSwipeBack(activity: Activity?, func: SwipeLayout.OnSwipedListener?) {
      val window = activity?.window ?: return
      val decor = window.decorView as? ViewGroup ?: return

      if (decor.childCount > 0) {
        val decorChild = decor.getChildAt(0)
        decor.removeView(decorChild)
        mSwipeLayout.addView(decorChild)
      }
      decor.addView(mSwipeLayout)

      mSwipeLayout.setOnSwipedListener(object : SwipeLayout.OnSwipedListener {
        override fun onSwipeFinish() {
          activity.finish()
          activity.overridePendingTransition(0, 0)
          func?.onSwipeFinish()
        }

        override fun onSwipeProgress(progress: Float) {
          val alpha = (255 * 0.5 * (1 - progress)).toInt()
          mSwipeLayout.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
          func?.onSwipeProgress(progress)
        }
      })
    }
  }
}
