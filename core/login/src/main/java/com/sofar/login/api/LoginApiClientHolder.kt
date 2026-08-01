package com.sofar.login.api

import com.sofar.core.network.ApiClient

object LoginApiClientHolder {

  private val apiClient: ApiClient by lazy {
    ApiClient(
      baseUrl = "http://musicapi.qianqian.com/",
      debugMode = true
    )
  }

  @JvmStatic
  val loginApiService: LoginApiService by lazy {
    apiClient.create<LoginApiService>()
  }

}