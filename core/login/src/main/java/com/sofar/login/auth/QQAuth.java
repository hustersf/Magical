package com.sofar.login.auth;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.sofar.base.BaseActivity;
import com.sofar.base.callback.ActivityCallback;
import com.sofar.login.activity.QQSSOActivity;
import com.sofar.login.model.User;
import com.sofar.social.sdk.QQConfig;
import com.tencent.tauth.Tencent;

import org.json.JSONObject;

import io.reactivex.Observable;

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
  public Observable<String> requestAuth() {
    return Observable.create(emitter -> {
      Intent intent = new Intent(context, QQSSOActivity.class);
      if (context instanceof BaseActivity) {
        ((BaseActivity) context).startActivityForResult(intent, new ActivityCallback() {
          @Override
          public void onResult(Intent data) {
            token = data.getStringExtra(QQSSOActivity.KEY_TOKEN);
            openId = data.getStringExtra(QQSSOActivity.KEY_OPEN_ID);
            expiresIn = data.getLongExtra(QQSSOActivity.KEY_EXPIRES_IN, 0);
            emitter.onNext(token);
            emitter.onComplete();
          }

          @Override
          public void onCancel(Intent data) {
            emitter.onError(new Throwable("login cancel"));
            emitter.onComplete();
          }
        });
      } else {
        emitter.onError(new Throwable("context must be BaseActivity"));
        emitter.onComplete();
      }
    });
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
