package com.sofar.core.download

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo

class DownloadNotificationHelper(
  private val context: Context,
  private val notificationId: Int,
  private val smallIconRes: Int = R.drawable.stat_sys_download, // 默认系统图标
  private val channelId: String = "download_channel_foreground",
  private val channelName: String = "File Downloads"
) {

  init {
    val channel = NotificationChannel(
      channelId,
      channelName,
      NotificationManager.IMPORTANCE_LOW
    ).apply { description = "Notifications for file downloading" }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
  }

  /**
   * 生成通用前台信息
   * @param title 通知标题
   * @param progress 当前进度 (0-100)
   * @param contentText 内容文本（如 "Downloading..."），若为空则显示百分比进度
   * @param indeterminate 是否为不确定进度（转圈）
   * @param iconOverride 临时覆盖图标资源（若不传则使用构造函数中的默认值）
   * @param foregroundServiceType 前台服务类型，默认为 DATA_SYNC
   * @param clickIntent 点击通知时跳转的 Intent（可选）
   */
  fun getForegroundInfo(
    title: String,
    progress: Int,
    contentText: String? = null,
    indeterminate: Boolean = false,
    iconOverride: Int? = null,
    foregroundServiceType: Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
    clickIntent: Intent? = null
  ): ForegroundInfo {

    val displayContent = contentText ?: "Progress: $progress%"

    val builder = NotificationCompat.Builder(context, channelId)
      .setContentTitle(title)
      .setContentText(displayContent)
      .setSmallIcon(iconOverride ?: smallIconRes) // 使用传递进来的图标或默认值
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setProgress(100, progress, indeterminate)

    clickIntent?.let {
      val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        it,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      )
      builder.setContentIntent(pendingIntent)
    }

    return ForegroundInfo(
      notificationId,
      builder.build(),
      foregroundServiceType
    )
  }
}