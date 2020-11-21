package com.sofar.take.picture.api;

import com.sofar.network.retrofit.SofarRetrofitConfig;

import io.reactivex.schedulers.Schedulers;

/**
 * todo 更改接口baseUrl
 */
public class ApiRetrofitConfig extends SofarRetrofitConfig {

  public ApiRetrofitConfig() {
    super("http://musicapi.qianqian.com/", Schedulers.newThread());
  }
}
