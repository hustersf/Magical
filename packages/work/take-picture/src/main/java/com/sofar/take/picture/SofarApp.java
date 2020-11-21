package com.sofar.take.picture;

import android.app.Application;

import com.sofar.base.app.AppLifeManager;
import com.sofar.image.ImageManager;

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
    ImageManager.get().init(this);
  }


}
