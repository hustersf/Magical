package com.sofar.core.ai.edge.data.network

import com.sofar.network.ApiClient

object ApiClientHolder {

  private val apiClient: ApiClient by lazy {
    ApiClient(
      baseUrl = ApiConst.CONFIG_BASE_URL,
      debugMode = true
    )
  }

  val modelApiService: ModelApiService by lazy {
    apiClient.create()
  }
}