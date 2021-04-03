package com.sofar.aurora.retrofit;


import com.sofar.aurora.model.BaseResponse;
import com.sofar.aurora.retrofit.gson.Response;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 网络返回数据code != 22000 统一处理
 */
public class SofarExceptionConsumer implements Consumer<Object> {

  @Override
  public void accept(@NonNull Object o) throws Exception {
    if (o instanceof Response) {
      Response<?> response = (Response<?>) o;
      if (response.code() != 22000) {
        throw new SofarException(response);
      }
    } else if (o instanceof BaseResponse) {
      BaseResponse response = (BaseResponse) o;
      if (response.code != 22000) {
        throw new SofarException(response);
      }
    }
  }
}
