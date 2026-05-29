package com.sofar.core.common.extension

import android.content.Intent
import android.os.Build
import android.os.Parcelable

/**
 * 全版本兼容的 Parcelable 数据安全读取扩展
 */
inline fun <reified T : Parcelable> Intent.getParcelableCompat(key: String): T? {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Android 13+ 新 API
    getParcelableExtra(key, T::class.java)
  } else {
    // Android 13 以下老 API
    @Suppress("DEPRECATION")
    getParcelableExtra(key) as? T
  }
}