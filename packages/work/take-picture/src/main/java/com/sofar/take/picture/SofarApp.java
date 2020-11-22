package com.sofar.take.picture;

import android.app.Application;

import androidx.annotation.NonNull;

import com.sofar.base.app.AppLifeManager;
import com.sofar.image.ImageManager;
import com.sofar.take.picture.model.User;

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
    ImageManager.get().init(this);
  }


}
