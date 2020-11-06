package com.sofar.login.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.login.Account;
import com.sofar.login.R;
import com.sofar.login.model.User;

public class LoginActivity extends BaseActivity {

  public static final String KEY_USER = "user";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);

    ImageView qqLogin = findViewById(R.id.qq_login);
    qqLogin.setOnClickListener(v -> {
      Account.loginWithQQ(this)
        .subscribe(user -> {
          loginFinish(user);
        }, throwable -> {
          loginFailed();
        });
    });
  }

  private void loginFinish(User user) {
    Intent intent = new Intent();
    intent.putExtra(KEY_USER, user);
    setActivityResultOK(intent);
  }

  private void loginFailed() {
    setActivityResultCancel(null);
  }

}
