package com.sofar.aurora.retrofit;

import com.sofar.aurora.SofarApp;
import com.sofar.utility.ToastUtil;

public class ExceptionHandler {

  public static boolean handleException(Throwable e) {
    if (e instanceof SofarException) {
      ToastUtil.startShort(SofarApp.getAppContext(), "无法连接网络，请稍后再试。");
      return true;
    }
    return false;
  }
}
