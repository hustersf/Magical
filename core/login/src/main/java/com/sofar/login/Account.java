package com.sofar.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.sofar.login.api.LoginApiClientHolder;
import com.sofar.login.auth.QQAuth;
import com.sofar.login.model.User;
import com.sofar.social.sdk.QQConfig;

import io.reactivex.Observable;

/**
 * 登录入口类
 */
public class Account {

  private static final String TAG = "Account";

  private static final String KEY_USER_CACHE = "user_cache";

  private static final String SP_CONFIG = "account_config";

  /**
   * 从本地缓存中获取上次登录的用户
   */
  @NonNull
  public static User getCurrentUser(@NonNull Context context) {
    SharedPreferences sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
    String userJson = sp.getString(KEY_USER_CACHE, "");
    Gson gson = new Gson();
    User user = gson.fromJson(userJson, User.class);
    if (user == null) {
      user = new User();
    }
    return user;
  }

  /**
   * 退出登录
   */
  public static void logout(@NonNull Context context) {
    saveUser(context, null);
  }

  /**
   * QQ登录
   */
  public static Observable<User> loginWithQQ(QQAuth qqAuth) {
    return LoginApiClientHolder.getLoginApiService().loginWithQQ(
      QQConfig.APP_ID,
      qqAuth.getToken(),
      qqAuth.getOpenId()
    ).map(response -> {
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

  /**
   * 保存用户信息至本地
   */
  public static void saveUser(@NonNull Context context, @Nullable User user) {
    Gson gson = new Gson();
    String userJson = gson.toJson(user);
    SharedPreferences sp = context.getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
    sp.edit().putString(KEY_USER_CACHE, userJson).commit();
  }

}
