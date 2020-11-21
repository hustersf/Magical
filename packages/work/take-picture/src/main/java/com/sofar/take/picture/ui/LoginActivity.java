package com.sofar.take.picture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.R;
import com.sofar.take.picture.api.ApiProvider;
import com.sofar.utility.ToastUtil;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity {

  EditText phoneEt;
  EditText passwordEt;
  TextView loginTv;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);
    phoneEt = findViewById(R.id.phone);
    passwordEt = findViewById(R.id.password);
    loginTv = findViewById(R.id.login);


    loginTv.setOnClickListener(v -> {
      login();
    });
  }

  private void login() {
    String phone = phoneEt.getText().toString().trim();
    String password = passwordEt.getText().toString().trim();

    if (phone.length() != 11) {
      ToastUtil.startShort(this, "手机号必须是11位");
      return;
    }

    if (password.length() < 6) {
      ToastUtil.startShort(this, "密码至少6位");
      return;
    }

    ApiProvider.getApiService()
      .login(phone, password)
      .subscribe(s -> {

      }, throwable -> {

      });

    jumpToMain();
  }

  private void jumpToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

}
