package com.sofar.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import coil3.SingletonImageLoader
import coil3.dispose
import coil3.load
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.toBitmap

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
    listener(
      onStart = { request -> Log.d("CoilDebug", "开始加载: ${request.data}") },
      onError = { request, result -> Log.e("CoilDebug", "首次加载失败原因: ", result.throwable) },
      onSuccess = { request, result -> Log.d("CoilDebug", "加载成功") }
    )
  }
}

fun ImageView.clearImage() {
  this.dispose()
  this.setImageDrawable(null)
}

/**
 * @param context 上下文
 * @param url 图片网络地址
 * @param onSuccess 成功获取 Bitmap 后的 Lambda 回调（默认在主线程）
 */
fun fetchImage(
  context: Context,
  url: String?,
  onSuccess: (Bitmap) -> Unit
) {
  if (url.isNullOrEmpty()) return

  //  构建 Coil 3 请求
  val request = ImageRequest.Builder(context)
    .data(url)
    // 🚨 核心避坑点：必须禁用硬件加速位图 (Hardware Bitmap)
    // 因为旧的业务拿到这个 Bitmap 后要走 BlurUtil 高斯模糊和 BitmapUtil 像素裁剪。
    // 如果这里不设置为 false，后续对像素进行读写时必定会引发真机运行崩溃！
    .allowHardware(false)
    // 利用最简 target 直接拦截成功回调
    .target { image ->
      // 将 Coil 3 跨平台的 image 资产无缝转为 Android 标准的 Bitmap
      onSuccess(image.toBitmap())
    }
    .build()

  // 扔进 Coil 3 的全局唯一单例加载器入队执行
  SingletonImageLoader.get(context).enqueue(request)
}
