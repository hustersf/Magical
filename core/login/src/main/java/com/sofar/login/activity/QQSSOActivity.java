package com.sofar.login.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sofar.social.sdk.QQConfig;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

public class QQSSOActivity extends AppCompatActivity {

  private static final String TAG = "QQSSOActivity";

  //这两个key的值取自于qq返回的json,不能改变
  public static final String KEY_TOKEN = "access_token";
  public static final String KEY_OPEN_ID = "openid";
  public static final String KEY_EXPIRES_IN = "expires_in";

  @NonNull
  Tencent tencent;

  IUiListener listener = new IUiListener() {
    @Override
    public void onComplete(Object o) {
      try {
        JSONObject json = ((JSONObject) o);
        Log.d(TAG, "success=" + json.toString());
        authFinish(json.getString(KEY_TOKEN), json.getString(KEY_OPEN_ID), json.getLong(KEY_EXPIRES_IN));
      } catch (Exception e) {
        authFailed();
      }
    }

    @Override
    public void onError(UiError uiError) {
      Log.d(TAG, "error=" + uiError.errorCode + ":" + uiError.errorMessage);
      authFailed();
    }

    @Override
    public void onCancel() {
      Log.d(TAG, "login cancel");
      authFailed();
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    tencent = Tencent.createInstance(QQConfig.APP_ID, getApplicationContext());

    if (!tencent.isSessionValid()) {
      requestLogin();
    } else {
      String accessToken = tencent.getAccessToken();
      String openId = tencent.getOpenId();
      long expiresIn = tencent.getExpiresIn();
      authFinish(accessToken, openId, expiresIn);
    }
  }

  private void requestLogin() {
    tencent.login(this, QQConfig.SCOPE, listener);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    tencent.handleResultData(data, listener);
  }

  private void authFinish(String token, String openId, long expiresIn) {
    Intent intent = new Intent();
    intent.putExtra(KEY_TOKEN, token);
    intent.putExtra(KEY_OPEN_ID, openId);
    intent.putExtra(KEY_EXPIRES_IN, expiresIn);
    setResult(RESULT_OK, intent);
    finish();
  }

  private void authFailed() {
    setResult(RESULT_CANCELED);
    finish();
  }

}
