package com.sofar.network.retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.sofar.network.cookie.SimpleCookieJar;
import com.sofar.network.https.HttpsUtil;
import com.sofar.network.interceptor.ContentLengthInterceptor;
import com.sofar.network.interceptor.HeadersInterceptor;
import com.sofar.network.interceptor.ParamsInterceptor;
import com.sofar.network.retrofit.consumer.NetworkCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;

public class SofarRetrofitConfig implements RetrofitConfig {

  protected static final int DEFAULT_TIME_OUT = 30;// 超时时间 30s

  String baseUrl;
  Scheduler scheduler;

  public SofarRetrofitConfig(@NonNull String baseUrl) {
    this(baseUrl, null);
  }

  public SofarRetrofitConfig(@NonNull String baseUrl, @Nullable Scheduler scheduler) {
    this.baseUrl = baseUrl;
    this.scheduler = scheduler;
  }

  @NonNull
  @Override
  public String buildBaseUrl() {
    return baseUrl;
  }

  @NonNull
  @Override
  public Params buildParams() {
    return new SofarParams();
  }

  @NonNull
  @Override
  public OkHttpClient buildClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.connectTimeout(timeout(), TimeUnit.SECONDS);// 连接超时时间
    builder.writeTimeout(timeout(), TimeUnit.SECONDS);// 写操作 超时时间
    builder.readTimeout(timeout(), TimeUnit.SECONDS);// 读操作超时时间

    Params params = buildParams();
    builder.addInterceptor(new HeadersInterceptor(params));
    builder.addInterceptor(new ParamsInterceptor(params));
    builder.addInterceptor(new ContentLengthInterceptor());
    for (Interceptor interceptor : interceptors()) {
      builder.addInterceptor(interceptor);
    }

    builder.cookieJar(new SimpleCookieJar());

    if (ignoreCertVerify()) {
      builder.sslSocketFactory(HttpsUtil.ignoreAllSocketFactory().first,
        HttpsUtil.ignoreAllSocketFactory().second);
    }
    return builder.build();
  }

  protected int timeout() {
    return DEFAULT_TIME_OUT;
  }

  @NonNull
  @Override
  public Gson buildGson() {
    return new Gson();
  }

  @Nullable
  @Override
  public Scheduler buildScheduler() {
    return scheduler;
  }

  @NonNull
  @Override
  public Call<Object> buildCall(Call<Object> call) {
    return call;
  }

  @NonNull
  @Override
  public Observable<?> buildObservable(Observable<?> input, Call<Object> call) {
    return input.observeOn(AndroidSchedulers.mainThread())
      .doOnComplete(NetworkCounter.ON_COMPLETE)
      .doOnError(NetworkCounter.ON_ERROR);
  }

  protected boolean ignoreCertVerify() {
    return false;
  }

  @NonNull
  protected List<Interceptor> interceptors() {
    return new ArrayList<>();
  }
}
