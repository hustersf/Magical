package com.sofar.core.image

import android.net.Uri
import android.widget.ImageView
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder

fun ImageView.loadImage(
  data: Any?, // 参数名改为 data，语义更通用（不再局限于 url 或 uri）
) {
  // 严谨的空判断：过滤掉 null、空字符串、以及空的 Uri
  if (data == null ||
    (data is String && data.isEmpty()) ||
    (data is Uri && data == Uri.EMPTY)
  ) {
    this.setImageResource(R.drawable.core_img_default_placeholder)
    return
  }

  this.load(data) {
    crossfade(true)
    placeholder(R.drawable.core_img_default_placeholder)
    error(R.drawable.core_img_default_error)
  }
}