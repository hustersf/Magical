package com.sofar.wan.android.utility

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.sofar.utility.DeviceUtil
import com.sofar.wan.android.App

object CommonUtil {

  fun context(): Context {
    return App.getAppContext()
  }

  fun dip2px(dip: Float): Int {
    return DeviceUtil.dp2px(context(), dip)
  }

  fun dimen(@DimenRes res: Int): Int {
    return context().resources.getDimensionPixelOffset(res)
  }

  fun color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(context(), res)
  }

  fun colors(@ColorRes res: Int): ColorStateList? {
    return ContextCompat.getColorStateList(context(), res)
  }

  fun drawable(@DrawableRes res: Int): Drawable? {
    return ContextCompat.getDrawable(context(), res)
  }
}