package com.sofar.take.picture.core;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.sofar.base.BaseActivity;
import com.sofar.base.callback.ActivityCallback;
import com.sofar.utility.DateUtil;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 拍摄并返回图片路径
 */
public class OpenCameraObservable implements Function<Object, ObservableSource<String>> {

  @NonNull
  public BaseActivity activity;

  public OpenCameraObservable(@NonNull BaseActivity activity) {
    this.activity = activity;
  }

  @Override
  public ObservableSource<String> apply(@NonNull Object o) throws Exception {
    return Observable.create(emitter -> {

      String photoName = DateUtil.getCurrentTimeInString() + PhotoHelper.IMAGE_FORMAT;
      File file = new File(PhotoHelper.getPhotoDir(activity), photoName);
      Uri imageUri = PhotoHelper.getUriForFile(activity, file);
      Intent intent = new Intent();
      intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
