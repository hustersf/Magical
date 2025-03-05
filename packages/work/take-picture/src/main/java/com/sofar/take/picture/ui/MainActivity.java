package com.sofar.take.picture.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.base.R;

public class MainActivity extends BaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new MainFragment())
      .commitAllowingStateLoss();
  }


}
