package com.sofar.core.network

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

class ApiClient(
  private val baseUrl: String,
  private val debugMode: Boolean = false
) {

  private val engine: NetworkEngine by lazy {
    val interceptors = mutableListOf<Interceptor>().apply {
      if (debugMode) {
        add(HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        })
      }
    }

    NetworkEngine(
      baseUrl = baseUrl,
      interceptors = interceptors
    )
  }

  /**
   * 快速克隆方法：仅切换 BaseUrl
   */
  fun newBuilder(newBaseUrl: String): ApiClient {
    return ApiClient(newBaseUrl, debugMode)
  }

  fun <T : Any> create(serviceClass: Class<T>): T {
    return engine.create(serviceClass)
  }

  inline fun <reified T : Any> create(): T = create(T::class.java)
}
