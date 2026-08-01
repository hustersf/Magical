package com.sofar.core.ui.util

import android.content.Context
import android.content.res.Resources
import java.lang.reflect.Method

/**
 * 判断当前设备是否有虚拟导航栏 (NavigationBar)
 * 示例用法：if (context.hasNavigationBar) { ... }
 */
val Context.hasNavigationBar: Boolean
  get() {
    val rs: Resources = resources
    val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
    var hasNavBar = if (id > 0) rs.getBoolean(id) else false

    try {
      val systemPropertiesClass = Class.forName("android.os.SystemProperties")
      val m: Method = systemPropertiesClass.getMethod("get", String::class.java)
      val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as? String

      if ("1" == navBarOverride) {
        hasNavBar = false
      } else if ("0" == navBarOverride) {
        hasNavBar = true
      }
    } catch (e: Exception) {
      // 忽略异常
    }
    return hasNavBar
  }

/**
 * 获取当前设备虚拟导航栏 (NavigationBar) 的高度
 * 示例用法：val height = context.navigationBarHeight
 */
val Context.navigationBarHeight: Int
  get() {
    val resources: Resources = resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
      resources.getDimensionPixelSize(resourceId)
    } else {
      0
    }
  }


/** 获取屏幕宽度 px */
val Context.screenWidth: Int
  get() = resources.displayMetrics.widthPixels

/** 获取屏幕高度 px */
val Context.screenHeight: Int
  get() = resources.displayMetrics.heightPixels

/** 获取手机屏幕 dpi */
val Context.densityDpi: Int
  get() = resources.displayMetrics.densityDpi

/** dp 和 px 的转化比例 (density) */
val Context.density: Float
  get() = resources.displayMetrics.density
