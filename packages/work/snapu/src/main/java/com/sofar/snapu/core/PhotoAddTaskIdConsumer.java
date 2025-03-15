package com.sofar.snapu.core;

import com.sofar.snapu.model.ImageInfo;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 给图片添加上 所属任务的id
 */
public class PhotoAddTaskIdConsumer implements Consumer<ImageInfo> {

  @NonNull
  public PhotoHelper helper;

  public PhotoAddTaskIdConsumer(@NonNull PhotoHelper helper) {
    this.helper = helper;
  }

  @Override
  public void accept(ImageInfo imageInfo) throws Exception {
    imageInfo.taskId = helper.taskId;
  }
}
