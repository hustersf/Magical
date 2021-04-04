package com.sofar.aurora.retrofit;

import java.io.IOException;
import java.util.TreeSet;

import com.sofar.utility.cipher.MD5Util;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SignParamInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    Request.Builder requestBuilder = request.newBuilder();
    HttpUrl originUrl = request.url();

    StringBuffer sb = new StringBuffer();
    TreeSet<String> urlParams = new TreeSet<>(originUrl.queryParameterNames());
    int i = 0;
    for (String key : urlParams) {
      sb.append(key);
      sb.append("=");
      sb.append(originUrl.queryParameter(key));
      if (i == urlParams.size() - 1) {
        sb.append("0b50b02fd0d73a9c4c8c3a781c30845f");
      } else {
        sb.append("&");
      }
      i++;
    }

    HttpUrl newUrl = originUrl.newBuilder()
      .addQueryParameter("sign", MD5Util.md5(sb.toString()))
      .build();
    requestBuilder.url(newUrl);
    request = requestBuilder.build();
    return chain.proceed(request);
  }
}
