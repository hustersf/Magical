package com.sofar.take.picture.api;

import androidx.annotation.NonNull;

import com.sofar.network.retrofit.RetrofitConfig;
import com.sofar.take.picture.SofarApp;

import java.util.HashMap;
import java.util.Map;

public class TaskPhotoParams implements RetrofitConfig.Params {

  @NonNull
  @Override
  public Map<String, String> getHeaderParams() {
    Map<String, String> params = new HashMap<>();
    return params;
  }

  @NonNull
  @Override
  public Map<String, String> getUrlParams() {
    Map<String, String> params = new HashMap<>();
    if (SofarApp.ME != null) {
      params.put("userId", SofarApp.ME.userId);
    }
    return params;
  }

  @NonNull
  @Override
  public Map<String, String> getBodyParams() {
    Map<String, String> params = new HashMap<>();
    return params;
  }

}
