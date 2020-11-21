package com.sofar.take.picture.core;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.sofar.base.BaseActivity;
import com.sofar.base.callback.ActivityCallback;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 拍摄并返回图片路径
 */
public class OpenCameraObservable implements Function<Long, ObservableSource<String>> {

  @NonNull
  public PhotoHelper helper;
  public int order;

  public OpenCameraObservable(@NonNull PhotoHelper helper, int order) {
    this.helper = helper;
    this.order = order;
  }

  @Override
  public ObservableSource<String> apply(@NonNull Long taskId) throws Exception {
    return Observable.create(emitter -> {

      String photoName = helper.getPhotoName(taskId, order) + PhotoHelper.IMAGE_FORMAT;
      File file = new File(helper.getPhotoDir(), photoName);
      Uri imageUri = helper.getUriForFile(file);
      Intent intent = new Intent();
      intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      BaseActivity activity = (BaseActivity) helper.activity;
      activity.startActivityForResult(intent, new ActivityCallback() {
        @Override
        public void onResult(Intent data) {
          emitter.onNext(file.getAbsolutePath());
          emitter.onComplete();
        }

        @Override
        public void onCancel(Intent data) {
          emitter.onError(new Throwable("cancel"));
          emitter.onComplete();
        }
      });
    });
  }

}
