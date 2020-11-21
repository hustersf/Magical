package com.sofar.take.picture.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.R;
import com.sofar.take.picture.core.PhotoObserveProvider;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity {

  Button takeBtn;

  Button photoBtn;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_activity);

    takeBtn = findViewById(R.id.take);
    takeBtn.setOnClickListener(v -> {
      carma();
    });

    photoBtn = findViewById(R.id.photo);
    photoBtn.setOnClickListener(v -> {
      photoList();
    });
  }

  private void carma() {
    PhotoObserveProvider.start(this);
  }

  private void photoList() {
    PhotoListActivity.launch(this);
  }
}
