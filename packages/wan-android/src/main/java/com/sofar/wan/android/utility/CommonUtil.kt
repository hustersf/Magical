package com.sofar.wan.android.utility

import android.content.Context
import com.sofar.utility.DeviceUtil
import com.sofar.wan.android.App

object CommonUtil {

  fun context(): Context {
    return App.getAppContext()
  }

  fun dip2px(dip: Float): Int {
    return DeviceUtil.dp2px(context(), dip)
  }
}