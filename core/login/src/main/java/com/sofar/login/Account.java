package com.sofar.login;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.sofar.base.BaseActivity;
import com.sofar.base.callback.ActivityCallback;
import com.sofar.login.api.LoginApiProvider;
import com.sofar.login.auth.QQAuth;
import com.sofar.login.model.User;
import com.sofar.login.ui.LoginActivity;
import com.sofar.social.sdk.QQConfig;
import com.sofar.utility.SharedPreferenceUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * 登录入口类
 */
public class Account {

  private static final String TAG = "Account";

  private static final String KEY_USER_CACHE = "user_cache";

  /**
   * 从本地缓存中获取上次登录的用户
   */
  @NonNull
  public static User getCurrentUser(@NonNull Context context) {
    SharedPreferenceUtil util = new SharedPreferenceUtil(context);
    String userJson = util.getToggleString(KEY_USER_CACHE);
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

  /**
   * 跳转至登录界面，登录完成后会返回一个User信息
   */
  public static Observable<User> loginActivity(@NonNull Context context) {
    return Observable.create((ObservableOnSubscribe<User>) emitter -> {
      Intent intent = new Intent(context, LoginActivity.class);
      if (context instanceof BaseActivity) {
        ((BaseActivity) context).startActivityForResult(intent, new ActivityCallback() {
          @Override
          public void onResult(Intent data) {
            User user = (User) data.getSerializableExtra(LoginActivity.KEY_USER);
            emitter.onNext(user);
            emitter.onComplete();
            saveUser(context, user);
          }

          @Override
          public void onCancel(Intent data) {
            emitter.onError(new Throwable("login failed"));
            emitter.onComplete();
          }
        });
      } else {
        emitter.onError(new Throwable("context must be BaseActivity"));
        emitter.onComplete();
      }
    });
  }

  /**
   * 保存用户信息至本地
   */
  private static void saveUser(@NonNull Context context, @Nullable User user) {
    Gson gson = new Gson();
    String userJson = gson.toJson(user);
    SharedPreferenceUtil util = new SharedPreferenceUtil(context);
    util.setToggleString(KEY_USER_CACHE, userJson);
  }

}
