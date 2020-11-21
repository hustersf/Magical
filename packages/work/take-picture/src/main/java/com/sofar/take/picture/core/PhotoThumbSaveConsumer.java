package com.sofar.take.picture.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sofar.base.BaseActivity;
import com.sofar.take.picture.model.ImageInfo;
import com.sofar.utility.BitmapUtil;

import java.io.File;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 生成缩图络并保存
 */
public class PhotoThumbSaveConsumer implements Consumer<ImageInfo> {

  /**
   * 缩略图片最大字节数
   */
  private static final int THUMB_MAX_SIZE = 100; //kb

  @NonNull
  public BaseActivity activity;

  public PhotoThumbSaveConsumer(@NonNull BaseActivity activity) {
    this.activity = activity;
  }

  @Override
  public void accept(ImageInfo imageInfo) throws Exception {
    File imageFile = new File(PhotoHelper.getPhotoDir(activity), imageInfo.name);
    Bitmap srcBt = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
    Bitmap thumbBt = BitmapUtil.compressBitmap(srcBt, THUMB_MAX_SIZE);
    Log.d(PhotoHelper.TAG, "生成缩图图");

    File thumbFile = new File(PhotoHelper.getPhotoThumbDir(activity), imageFile.getName());
    PhotoHelper.saveBitmapFile(thumbBt, thumbFile);
  }
}
