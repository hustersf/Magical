package com.sofar.aurora.retrofit.api;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import com.sofar.network.retrofit.RetrofitConfig;
import com.sofar.utility.cipher.MD5Util;

public class SofarParams implements RetrofitConfig.Params {

  @NonNull
  @Override
  public Map<String, String> getHeaderParams() {
    Map<String, String> params = new HashMap<>();
    params.put("device-id", "fb6af8b9-12f0-4f1d-8184-f12ec996cf26simulate");
    params.put("app-version", "v8.2.3.3");
    params.put("channel", "Mi");
    params.put("from", "android");
    return params;
  }

  @NonNull
  @Override
  public Map<String, String> getUrlParams() {
    Map<String, String> params = new HashMap<>();
    params.put("appid", "16073360");
    params.put("timestamp", System.currentTimeMillis() + "");
    return params;
  }

  @NonNull
  @Override
  public Map<String, String> getBodyParams() {
    Map<String, String> params = new HashMap<>();
    return params;
  }
}
