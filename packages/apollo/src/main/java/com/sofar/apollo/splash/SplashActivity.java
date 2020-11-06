package com.sofar.apollo.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.sofar.apollo.MainActivity;
import com.sofar.apollo.SofarApp;
import com.sofar.base.BaseActivity;
import com.sofar.login.Account;
import com.sofar.utility.ToastUtil;

public class SplashActivity extends BaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (SofarApp.isLogin()) {
      gotoMain();
    } else {
      gotoLogin();
    }
  }

  private void gotoMain() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  private void gotoLogin() {
    Account.loginActivity(this)
      .subscribe(user -> {
        SofarApp.ME = user;
        gotoMain();
      }, throwable -> {
        finish();
        ToastUtil.startShort(this, "登录失败");
      });
  }
}
