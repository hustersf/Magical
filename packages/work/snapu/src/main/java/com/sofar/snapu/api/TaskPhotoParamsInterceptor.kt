package com.sofar.snapu.api

import com.sofar.snapu.SofarApp
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 公共参数拦截器
 */
class TaskPhotoParamsInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    // 判断并获取 userId 资产（对应原 getUrlParams 逻辑）
    val userId = SofarApp.ME?.userId

    // 如果已登录，则动态修改 URL，追加 userId 参数
    val newRequest = if (!userId.isNullOrEmpty()) {
      val newUrl = originalRequest.url.newBuilder()
        .addQueryParameter("userId", userId)
        .build()

      originalRequest.newBuilder()
        .url(newUrl)
        .build()
    } else {
      originalRequest // 未登录则原样发送请求
    }

    return chain.proceed(newRequest)
  }
}
