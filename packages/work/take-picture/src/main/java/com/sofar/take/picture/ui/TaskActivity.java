package com.sofar.take.picture.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.R;
import com.sofar.take.picture.core.PhotoHelper;
import com.sofar.take.picture.core.PhotoObserveProvider;
import com.sofar.utility.DateUtil;
import com.sofar.utility.ToastUtil;

public class TaskActivity extends BaseActivity {

  TextView taskTimeTv;
  long taskId;

  TextView photoTv;
  TextView cameraTv;
  TextView finishTv;

  @NonNull
  PhotoObserveProvider provider;

  @NonNull
  PhotoHelper helper;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.task_activity);
    taskId = System.currentTimeMillis();
    provider = new PhotoObserveProvider();
    helper = new PhotoHelper(this, taskId);
    initView();
  }

  private void initView() {
    taskTimeTv = findViewById(R.id.task_time);
    taskTimeTv.setText("创建任务时间 " + DateUtil.getTime(taskId));

    photoTv = findViewById(R.id.photo);
    cameraTv = findViewById(R.id.camera);
    finishTv = findViewById(R.id.finish);

    photoTv.setOnClickListener(v -> PhotoListActivity.launch(this, taskId));
    cameraTv.setOnClickListener(v -> startCamera());
    finishTv.setOnClickListener(v -> finish());
  }

  private void startCamera() {
    String des = "拍照权限被禁止，我们需要打开拍照权限";
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    helper.deleteAllFile();
    ToastUtil.startShort(this, "已删除当前任务拍摄的图片");
  }
}
