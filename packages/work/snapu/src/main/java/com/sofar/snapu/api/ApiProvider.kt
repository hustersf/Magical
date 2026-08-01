package com.sofar.snapu.api

import com.sofar.network.ApiClient

object ApiProvider {

  const val baseUrl: String = "http:47.93.240.220:5000/"
  private val apiClient: ApiClient by lazy {
    ApiClient(
      baseUrl = baseUrl,
      customInterceptors = listOf(
        TaskPhotoParamsInterceptor(),
      ),
      debugMode = true // 建议实际生产替换为 BuildConfig.DEBUG
    )
  }

  // 统一暴露业务需要的 ApiService，并加上注解完美兼容现有的 Java 调用方
  @JvmStatic
  val apiService: ApiService by lazy {
    apiClient.create()
  }
}
