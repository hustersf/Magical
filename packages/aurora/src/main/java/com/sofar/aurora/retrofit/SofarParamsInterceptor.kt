package com.sofar.aurora.retrofit

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 承接原 SofarParams 的公共参数拦截器
 */
class SofarParamsInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    // 动态添加原 getUrlParams() 的公共 URL 参数
    val newUrl = originalRequest.url.newBuilder()
      .addQueryParameter("appid", "16073360")
      .addQueryParameter("timestamp", System.currentTimeMillis().toString())
      .build()

    // 动态添加原 getHeaderParams() 的公共请求头
    val newRequest = originalRequest.newBuilder()
      .url(newUrl)
      .addHeader("device-id", "fb6af8b9-12f0-4f1d-8184-f12ec996cf26simulate")
      .addHeader("app-version", "v8.2.3.3")
      .addHeader("channel", "Mi")
      .addHeader("from", "android")
      .build()

    return chain.proceed(newRequest)
  }
}
