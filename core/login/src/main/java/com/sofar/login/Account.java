package com.sofar.login;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sofar.login.api.LoginApiProvider;
import com.sofar.login.auth.QQAuth;
import com.sofar.login.model.User;
import com.sofar.social.sdk.QQConfig;

import io.reactivex.Observable;

/**
 * 登录入口类
 */
public class Account {

  private static final String TAG = "Account";

  /**
   * QQ登录
   */
  public static Observable<User> loginWithQQ(@NonNull Context context) {
    QQAuth qqAuth = new QQAuth(context);
    return qqAuth.requestAuth()
      .flatMap(s -> {
        Log.d(TAG, qqAuth.getToken() + " : " + qqAuth.getOpenId());
        return LoginApiProvider.getLoginApiService().loginWithQQ(QQConfig.APP_ID, qqAuth.getToken(), qqAuth.getOpenId());
      }).map(response -> {
        User user = new User();
        user.userId = qqAuth.getOpenId();
        user.name = response.name;
        user.headUrl = response.headUrl;
        user.gender = response.gender;
        return user;
      }).doOnError(throwable -> {
        Log.d(TAG, "login qq error=" + throwable.toString());
      });

  }


}
