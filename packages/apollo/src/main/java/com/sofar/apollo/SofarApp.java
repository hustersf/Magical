package com.sofar.apollo;

import android.app.Application;

import com.sofar.base.app.AppLifeManager;

public class SofarApp extends Application {

  private static SofarApp theApp;

  public static SofarApp getAppContext() {
    return theApp;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    theApp = this;
    AppLifeManager.get().init(this);
  }
}
