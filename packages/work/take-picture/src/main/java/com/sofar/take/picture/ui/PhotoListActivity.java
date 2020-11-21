package com.sofar.take.picture.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.R;

/**
 * 图片列表页面
 */
public class PhotoListActivity extends BaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new PhotoListFragment())
      .commitAllowingStateLoss();
  }

  public static void launch(@NonNull Activity activity) {
    Intent intent = new Intent(activity, PhotoListActivity.class);
    activity.startActivity(intent);
  }
}
