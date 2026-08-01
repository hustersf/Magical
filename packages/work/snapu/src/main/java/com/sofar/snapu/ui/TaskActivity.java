package com.sofar.snapu.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import com.sofar.core.common.extension.DateExtKt;
import com.sofar.snapu.R;
import com.sofar.snapu.core.PhotoHelper;
import com.sofar.snapu.core.PhotoObserveProvider;

public class TaskActivity extends AppCompatActivity {

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
    taskTimeTv.setText("创建任务时间 " + DateExtKt.toDateTimeString(taskId));

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
    Toast.makeText(this, "已删除当前任务拍摄的图片", Toast.LENGTH_SHORT).show();
  }
}
