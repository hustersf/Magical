package com.sofar.aurora.retrofit.api;

import com.sofar.network.retrofit.RetrofitFactory;

public class ApiProvider {

  private static class Inner {
    private final static ApiService INSTANCE = RetrofitFactory.newBuilder(new ApiRetrofitConfig())
      .build().create(ApiService.class);
  }

  public static ApiService getApiService() {
    return Inner.INSTANCE;
  }
}
