package com.sofar.snapu;

import android.app.Application;

import com.sofar.core.common.lifecycle.AppLifeManager;
import com.sofar.snapu.model.User;

public class SofarApp extends Application {

  private static SofarApp theApp;

  public static User ME;

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
