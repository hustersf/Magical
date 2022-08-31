package com.sofar.wan.android.network.api

import com.sofar.network.retrofit.RetrofitFactory
import com.sofar.network.retrofit.SofarRetrofitConfig

object ApiProvider {

  private val apiService by lazy {
    RetrofitFactory
      .newBuilder(SofarRetrofitConfig("https://www.wanandroid.com/"))
      .build()
      .create(ApiService::class.java)
  }

  fun get(): ApiService {
    return apiService
  }
}