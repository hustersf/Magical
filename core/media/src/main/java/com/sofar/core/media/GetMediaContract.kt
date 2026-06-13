package com.sofar.core.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat

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
 * 返回参数：[List<Uri>] 选择或拍摄后的 Uri 列表（统一转为 List 方便上层处理）
 */
class GetMediaContract : ActivityResultContract<MediaAction, List<Uri>>() {

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

  override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
    if (resultCode != Activity.RESULT_OK || intent == null) {
      return emptyList()
    }

    val uriList = mutableListOf<Uri>()

    // 1. 优先尝试解析多选结果（对应你代码中的 finishWithListResult）
    val list = IntentCompat.getParcelableArrayListExtra(
      intent,
      MediaLauncherActivity.EXTRA_SELECTED_URIS,
      Uri::class.java
    )
    if (list != null) {
      uriList.addAll(list)
    }

    // 2. 如果多选为空，尝试解析单选/拍照结果（对应你代码中的 finishWithResult）
    if (uriList.isEmpty()) {
      intent.data?.let { uri ->
        uriList.add(uri)
      }
    }
    return uriList
  }
}