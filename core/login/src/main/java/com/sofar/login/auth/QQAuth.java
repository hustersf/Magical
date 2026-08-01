package com.sofar.login.auth;

import org.json.JSONObject;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.sofar.login.model.User;
import com.sofar.social.sdk.QQConfig;
import com.tencent.tauth.Tencent;

/**
 * 拿到token 和 openId后 可获取用户授权的信息
 */
public class QQAuth extends AuthClient {

  String token;
  String openId;
  long expiresIn;


  public QQAuth(@NonNull Context context) {
    super(context);
  }

  @Override
  public boolean isAvailable() {
    Tencent tencent = Tencent.createInstance(QQConfig.APP_ID, context.getApplicationContext());
    return tencent.isQQInstalled(context.getApplicationContext());
  }

  /**
   * 获取用户信息
   */
  @WorkerThread
  public User getUserInfo() throws Exception {
    Tencent tencent = Tencent.createInstance(QQConfig.APP_ID, context.getApplicationContext());
    tencent.setAccessToken(token, String.valueOf(expiresIn));
    tencent.setOpenId(openId);
    JSONObject json = tencent.request(QQConfig.SCOPE, null, "GET");

    return new User();
  }

  @Nullable
  public String getOpenId() {
    return openId;
  }

  @Nullable
  public String getToken() {
    return token;
  }
}
