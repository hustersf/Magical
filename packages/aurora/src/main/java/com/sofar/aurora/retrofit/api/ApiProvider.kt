package com.sofar.aurora.retrofit.api

import com.sofar.network.ApiClient
import com.sofar.aurora.retrofit.SignParamInterceptor
import com.sofar.aurora.retrofit.SofarParamsInterceptor
import com.sofar.aurora.retrofit.gson.Gsons

object ApiProvider {

  private val apiClient: ApiClient by lazy {
    ApiClient(
      baseUrl = "https://api-qianqian.taihe.com/",
      customInterceptors = listOf(
        SofarParamsInterceptor(),
        SignParamInterceptor()
      ),
      gson = Gsons.STANDARD_GSON,
      debugMode = true // 建议实际生产替换为 BuildConfig.DEBUG
    )
  }

  // 统一暴露业务需要的 ApiService，并加上注解完美兼容现有的 Java 调用方
  @JvmStatic
  val apiService: ApiService by lazy {
    apiClient.create()
  }
}
