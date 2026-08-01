package com.sofar.wan.android.network.api

import com.sofar.network.ApiClient

object ApiProvider {

  private val apiClient by lazy {
    ApiClient(
      baseUrl = "https://www.wanandroid.com/",
      debugMode = true // 建议实际生产替换为 BuildConfig.DEBUG
    )
  }

  // 统一对外暴露延迟初始化的 ApiService
  @JvmStatic
  val apiService: ApiService by lazy {
    apiClient.create()
  }

  @JvmStatic
  fun get(): ApiService {
    return apiService
  }
}
