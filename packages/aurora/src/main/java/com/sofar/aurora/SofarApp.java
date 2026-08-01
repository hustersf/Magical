package com.sofar.aurora;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.sofar.aurora.rxjava.SofarErrorConsumer;
import com.sofar.core.common.lifecycle.AppLifeManager;

import io.reactivex.plugins.RxJavaPlugins;

public class SofarApp extends Application {

  private static SofarApp theApp;
  private static boolean isDebug;

  public static SofarApp getAppContext() {
    return theApp;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    isDebug = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    theApp = this;
    AppLifeManager.get().init(this);
    RxJavaPlugins.setErrorHandler(new SofarErrorConsumer(isDebug));
  }


}
