package com.sofar.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sofar.base.callback.ActivityCallback;

public class BaseActivity extends AppCompatActivity {

  // 设置启动和退出Activity的动画
  public static final String START_EXIT_PAGE_ANIMATION = "start_exit_page_animation";
  public static final String START_ENTER_PAGE_ANIMATION = "start_enter_page_animation";
  public static final String FINISH_EXIT_PAGE_ANIMATION = "finish_exit_page_animation";
  public static final String FINISH_ENTER_PAGE_ANIMATION = "finish_enter_page_animation";
  public static final int NO_ANIM = 0;

  // activity请求回调相关
  private SparseArray<ActivityCallback> callbacks = new SparseArray<>();
  private static final int REQUEST_CODE = 100;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  /**
   * activity请求回调
   * 继承自该activity的页面，统一调用该方法
   */
  public void startActivityForResult(Intent intent, ActivityCallback callback) {
    callbacks.put(REQUEST_CODE, callback);
    startActivityForResult(intent, REQUEST_CODE);
  }

  /**
   * activity请求回调
   * 继承自该activity的页面，统一调用该方法
   */
  public void startActivityForResult(Intent intent, int requestCode, ActivityCallback callback) {
    callbacks.put(requestCode, callback);
    startActivityForResult(intent, requestCode);
  }

  /**
   * Activity回调成功
   */
  public void setActivityResultOK(@Nullable Intent intent) {
    if (intent == null) {
      setResult(Activity.RESULT_OK, new Intent());
    } else {
      setResult(Activity.RESULT_OK, intent);
    }
    finish();
  }

  /**
   * Activity回调失败
   */
  public void setActivityResultCancel(@Nullable Intent intent) {
    if (intent == null) {
      setResult(Activity.RESULT_CANCELED, new Intent());
    } else {
      setResult(Activity.RESULT_CANCELED, intent);
    }
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ActivityCallback callback = callbacks.get(requestCode);
    callbacks.remove(requestCode);
    if (callback == null) {
      return;
    }

    if (resultCode == Activity.RESULT_OK) {
      callback.onResult(data);
    } else {
      callback.onCancel(data);
    }
  }

  @Override
  public void startActivity(Intent intent) {
    super.startActivity(intent);
    overridePendingTransition(
      getIntent().getIntExtra(START_ENTER_PAGE_ANIMATION, R.anim.right_slide_in),
      getIntent().getIntExtra(START_EXIT_PAGE_ANIMATION, R.anim.placeholder_anim));
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(
      getIntent().getIntExtra(FINISH_ENTER_PAGE_ANIMATION, NO_ANIM),
      getIntent().getIntExtra(FINISH_EXIT_PAGE_ANIMATION, R.anim.right_slide_out));
  }
}
