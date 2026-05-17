package com.sofar.core.ai.edge.data.network

import com.sofar.core.network.ApiClient

object ApiClientHolder {

  private val instance: ApiClient by lazy {
    ApiClient(
      baseUrl = ApiConst.CONFIG_BASE_URL,
      debugMode = true
    )
  }

  fun client(): ApiClient {
    return instance
  }
}