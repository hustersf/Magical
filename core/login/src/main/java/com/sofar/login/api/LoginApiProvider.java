package com.sofar.login.api;

import com.sofar.network.retrofit.RetrofitFactory;

public class LoginApiProvider {

  private static class Inner {
    private final static LoginApiService INSTANCE = RetrofitFactory.newBuilder(new LoginApiRetrofitConfig())
      .build().create(LoginApiService.class);
  }

  public static LoginApiService getLoginApiService() {
    return Inner.INSTANCE;
  }
}
