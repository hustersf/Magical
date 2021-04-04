package com.sofar.aurora.retrofit.api;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sofar.aurora.retrofit.SignParamInterceptor;
import com.sofar.aurora.retrofit.SofarExceptionConsumer;
import com.sofar.aurora.retrofit.gson.Gsons;
import com.sofar.network.retrofit.SofarRetrofitConfig;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import retrofit2.Call;

public class ApiRetrofitConfig extends SofarRetrofitConfig {

  public static final String baseUrl = "https://api-qianqian.taihe.com/";

  public ApiRetrofitConfig() {
    super(baseUrl, Schedulers.newThread());
  }

  @NonNull
  @Override
  public Params buildParams() {
    return new SofarParams();
  }

  @Override
  protected boolean ignoreCertVerify() {
    return true;
  }

  @NonNull
  @Override
  protected List<Interceptor> interceptors() {
    List<Interceptor> list = new ArrayList<>();
    list.add(new SignParamInterceptor());
    return list;
  }

  @NonNull
  @Override
  public Gson buildGson() {
    return Gsons.STANDARD_GSON;
  }

  @NonNull
  @Override
  public Observable<?> buildObservable(Observable<?> input, Call<Object> call) {
    return super.buildObservable(input, call)
      .doOnNext(new SofarExceptionConsumer());
  }
}
