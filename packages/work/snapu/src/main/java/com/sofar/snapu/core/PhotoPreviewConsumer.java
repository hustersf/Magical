package com.sofar.snapu.core;

import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.ui.PhotoPreviewActivity;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 图片预览
 */
public class PhotoPreviewConsumer implements Consumer<ImageInfo> {

  @NonNull
  public PhotoHelper helper;

  public PhotoPreviewConsumer(@NonNull PhotoHelper helper) {
    this.helper = helper;
  }

  @Override
  public void accept(ImageInfo imageInfo) throws Exception {
    PhotoPreviewActivity.launch(helper.activity, imageInfo);
  }
}
