package com.sofar.apollo;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.sofar.base.app.AppLifeManager;
import com.sofar.image.ImageManager;
import com.sofar.login.Account;
import com.sofar.login.model.User;

public class SofarApp extends Application {

  private static SofarApp theApp;

  /**
   * 当前用户
   */
  @NonNull
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

    ME = Account.getCurrentUser(this);
  }

  /**
   * 判断是否登录
   */
  public static boolean isLogin() {
    return ME != null && !TextUtils.isEmpty(ME.userId);
  }
}
