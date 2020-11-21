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

  public static final String KEY_TASK_ID = "task_id";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    PhotoListFragment fragment = new PhotoListFragment();
    Bundle b = new Bundle();
    b.putLong(KEY_TASK_ID, getIntent().getLongExtra(KEY_TASK_ID, 0));
    fragment.setArguments(b);
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .commitAllowingStateLoss();
  }

  public static void launch(@NonNull Activity activity, long taskId) {
    Intent intent = new Intent(activity, PhotoListActivity.class);
    intent.putExtra(KEY_TASK_ID, taskId);
    activity.startActivity(intent);
  }
}
