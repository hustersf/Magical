package com.sofar.snapu.feature.base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.sofar.snapu.R

object DialogUtil {

  fun showRationaleDialog(
    context: Context,
    titleId: Int,
    messageId: Int,
    confirm: Runnable? = null
  ) {
    AlertDialog.Builder(context)
      .setTitle(titleId)
      .setMessage(messageId)
      .setPositiveButton(R.string.action_confirm) { _, _ ->
        confirm?.run()
      }
      .setNegativeButton(R.string.action_cancel, null)
      .show()
  }

  fun showGoToSettingsDialog(
    context: Context,
    titleId: Int,
    messageId: Int,
  ) {
    AlertDialog.Builder(context)
      .setTitle(titleId)
      .setMessage(messageId)
      .setPositiveButton(R.string.action_confirm) { _, _ ->
        // 跳转应用设置页
        startAppSettingActivity(context)
      }
      .setNegativeButton(R.string.action_cancel, null)
      .show()
  }

  private fun startAppSettingActivity(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
      intent.setData(Uri.fromParts("package", context.packageName, null))
      context.startActivity(intent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}