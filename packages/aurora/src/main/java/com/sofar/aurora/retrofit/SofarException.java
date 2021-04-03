package com.sofar.aurora.retrofit;

import java.io.IOException;

import com.sofar.aurora.model.BaseResponse;
import com.sofar.aurora.retrofit.gson.Response;

public class SofarException extends IOException {

  public final int mCode;
  public final String mMessage;

  public SofarException(Response<?> response) {
    mCode = response.code();
    mMessage = response.message();
  }

  public SofarException(BaseResponse response) {
    mCode = response.code;
    mMessage = response.message;
  }

  @Override
  public String getMessage() {
    return mMessage;
  }

  public int getErrorCode() {
    return mCode;
  }
}
