package com.sofar.mlkit

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Util {

  fun vibrate(context: Context, duration: Long) {
    val vibrator = context.getVibrator() ?: return
    if (vibrator.hasVibrator()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Android 8.0+ 使用 VibrationEffect
        vibrator.vibrate(
          VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        // 旧版本直接调用 vibrate()
        vibrator.vibrate(duration)
      }
    }
  }

  // 使用扩展函数优化调用
  fun Context.getVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
      getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

}