package com.sofar.take.picture.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.R;
import com.sofar.take.picture.SofarApp;
import com.sofar.take.picture.api.ApiProvider;
import com.sofar.take.picture.model.LoginRequest;
import com.sofar.take.picture.model.User;
import com.sofar.utility.ToastUtil;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity {

  EditText phoneEt;
  EditText passwordEt;
  TextView loginTv;

  ProgressDialog dialog;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);
    phoneEt = findViewById(R.id.phone);
    passwordEt = findViewById(R.id.password);
    loginTv = findViewById(R.id.login);

    initProgressDialog();

    loginTv.setOnClickListener(v -> {
      login();
    });
  }

  private void initProgressDialog() {
    dialog = new ProgressDialog(this);
    dialog.setTitle("登录中...");
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

    dialog.show();
    LoginRequest request = new LoginRequest();
    request.username = phone;
    request.password = password;
    ApiProvider.getApiService().login(request)
      .subscribe(s -> {
        dialog.dismiss();
        User user = new User();
        user.userId = phone;
        SofarApp.ME = user;
        jumpToMain();
      }, throwable -> {
        dialog.dismiss();
        ToastUtil.startShort(this, "登录失败");
      });

  }

  private void jumpToMain() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }

}
