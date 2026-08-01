package com.sofar.aurora.retrofit;

import android.widget.Toast;

import com.sofar.aurora.SofarApp;

public class ExceptionHandler {

  public static boolean handleException(Throwable e) {
    if (e instanceof SofarException) {
      Toast.makeText(SofarApp.getAppContext(), "无法连接网络，请稍后再试。", Toast.LENGTH_SHORT)
        .show();
      return true;
    }
    return false;
  }
}
