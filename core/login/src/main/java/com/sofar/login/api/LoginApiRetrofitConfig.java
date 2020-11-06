package com.sofar.login.api;

import com.sofar.network.retrofit.SofarRetrofitConfig;

import io.reactivex.schedulers.Schedulers;

/**
 * todo 更改接口baseUrl
 */
public class LoginApiRetrofitConfig extends SofarRetrofitConfig {

  public LoginApiRetrofitConfig() {
    super("http://musicapi.qianqian.com/", Schedulers.newThread());
  }
}
