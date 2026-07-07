package com.sofar.core.ui.util

import android.content.Context
import android.util.TypedValue

/**
 * Android 官方标准的 dp 转 px 方法 (返回 Float)
 * 适用于 Canvas 绘制、Paint 描边等需要高精度浮点数的场景，避免强制转 Int 导致像素错位
 */
fun Context.dp2px(dp: Float): Float {
  return TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
  )
}

fun Context.dp2pxInt(dp: Float): Int {
  val px = dp2px(dp)
  return (if (px >= 0) px + 0.5f else px - 0.5f).toInt()
}