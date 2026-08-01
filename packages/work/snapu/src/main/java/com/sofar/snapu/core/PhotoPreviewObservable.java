package com.sofar.snapu.core;

import androidx.appcompat.app.AppCompatActivity;
import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.ui.PhotoPreviewActivity;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 图片预览
 */
public class PhotoPreviewObservable implements Function<ImageInfo, ObservableSource<?>> {

  @NonNull
  public AppCompatActivity activity;
  @NonNull
  public PhotoHelper helper;

  public PhotoPreviewObservable(@NonNull PhotoHelper helper, @NonNull AppCompatActivity activity) {
    this.helper = helper;
    this.activity = activity;
  }


  @Override
  public ObservableSource<?> apply(@NonNull ImageInfo imageInfo) throws Exception {
    return Observable.create(emitter -> {
      PhotoPreviewActivity.launch(activity, imageInfo);
    });
  }
}
