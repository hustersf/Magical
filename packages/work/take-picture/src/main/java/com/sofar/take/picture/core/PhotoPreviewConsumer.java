package com.sofar.take.picture.core;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.model.ImageInfo;
import com.sofar.take.picture.ui.PhotoPreviewActivity;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 图片预览
 */
public class PhotoPreviewConsumer implements Consumer<ImageInfo> {

  @NonNull
  public BaseActivity activity;

  public PhotoPreviewConsumer(@NonNull BaseActivity activity) {
    this.activity = activity;
  }

  @Override
  public void accept(ImageInfo imageInfo) throws Exception {
    PhotoPreviewActivity.launch(activity, imageInfo);
  }
}
