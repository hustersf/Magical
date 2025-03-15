package com.sofar.snapu.api;

import androidx.annotation.NonNull;

import com.sofar.network.retrofit.SofarRetrofitConfig;

import io.reactivex.schedulers.Schedulers;

/**
 * todo 更改接口baseUrl
 */
public class ApiRetrofitConfig extends SofarRetrofitConfig {

  public static final String baseUrl = "http:47.93.240.220:5000/";

  public ApiRetrofitConfig() {
    super(baseUrl, Schedulers.newThread());
  }

  @NonNull
  @Override
  public Params buildParams() {
    return new TaskPhotoParams();
  }
}
