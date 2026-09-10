package com.sofar.feature.ai.edge.chat.impl.detail.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.sofar.core.ui.activity.BaseUIActivity
import com.sofar.feature.ai.edge.chat.impl.R
import com.sofar.image.loadImage
import com.sofar.core.res.view.R as coreViewR

class ImagePreviewActivity : BaseUIActivity() {

  companion object {
    private const val EXTRA_IMAGE_PATH = "extra_image_path"

    @JvmStatic
    fun launch(context: Context, path: String) {
      val intent = Intent(context, ImagePreviewActivity::class.java).apply {
        putExtra(EXTRA_IMAGE_PATH, path)
      }
      context.startActivity(intent)

      // Android 13 及以下设备，启动动画必须在发出端紧跟在 startActivity 后面调用
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE && context is Activity) {
        @Suppress("DEPRECATION")
        context.overridePendingTransition(coreViewR.anim.core_anim_fade_in, 0)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // 💡Android 14+ (API 34) 设备的启动动画，必须写在接收端自己的 onCreate 初始化节点中
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      overrideActivityTransition(
        OVERRIDE_TRANSITION_OPEN,
        coreViewR.anim.core_anim_fade_in, // 进入动画：淡入
        0 // 0：让前一个页面静止，不发生奇怪的滑动拉扯
      )
    }
    setContentView(R.layout.feature_chat_image_preview_activity)

    val path = intent.getStringExtra(EXTRA_IMAGE_PATH) ?: return finish()

    val previewIv: ImageView = findViewById(R.id.preview_iv)
    val closeBtn: View = findViewById(R.id.close_btn)

    previewIv.loadImage(path)

    previewIv.setOnClickListener { finish() }
    closeBtn.setOnClickListener { finish() }
  }

  // 退出动画截断：当用户点击返回、或者点击大图退出时，大图淡出隐去
  override fun finish() {
    super.finish()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      overrideActivityTransition(
        OVERRIDE_TRANSITION_CLOSE,
        0,
        coreViewR.anim.core_anim_fade_out
      )
    } else {
      @Suppress("DEPRECATION")
      overridePendingTransition(0, coreViewR.anim.core_anim_fade_out)
    }
  }
}