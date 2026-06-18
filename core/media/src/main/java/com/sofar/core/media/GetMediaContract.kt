package com.sofar.core.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * 媒体文件操作类型枚举
 */
enum class MediaAction {
  PICK_IMAGE, // 相册多选
  TAKE_PHOTO  // 相机拍照
}

/**
 * 自定义媒体选择/拍摄契约
 * 输入参数：[MediaAction] 指令
 * 返回参数：[List<String>] 选择或拍摄后的本地沙盒永久物理路径列表（统一转为路径方便上层直接操作文件）
 */
class GetMediaContract : ActivityResultContract<MediaAction, List<String>>() {

  companion object {
    /**
     * 对应 MediaLauncherActivity 中解析行为的 Key
     */
    const val EXTRA_MEDIA_ACTION = "EXTRA_MEDIA_ACTION"
  }

  override fun createIntent(context: Context, input: MediaAction): Intent {
    return Intent(context, MediaLauncherActivity::class.java).apply {
      putExtra(EXTRA_MEDIA_ACTION, input.name)
    }
  }

  override fun parseResult(resultCode: Int, intent: Intent?): List<String> {
    if (resultCode != Activity.RESULT_OK || intent == null) {
      return emptyList()
    }

    return intent.getStringArrayListExtra(MediaLauncherActivity.EXTRA_SELECTED_PATHS)
      ?: emptyList()
  }
}