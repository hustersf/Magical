package com.sofar.snapu.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import com.sofar.utility.BitmapUtil;

import java.io.File;
import java.io.IOException;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 解决拍照的图片旋转90度问题
 * <p>
 * 小米9 得到的旋转角度是180度，但实际上是旋转了90度，此手机系统相册也有此问题
 */
public class PhotoRotateConsumer implements Consumer<String> {

  @NonNull
  public PhotoHelper helper;

  public PhotoRotateConsumer(@NonNull PhotoHelper helper) {
    this.helper = helper;
  }

  @Override
  public void accept(@NonNull String srcPath) throws Exception {
    int degree = readPictureDegree(srcPath);
    Log.d(PhotoHelper.TAG, "图片旋转角度=" + degree);
    Bitmap srcBt = BitmapFactory.decodeFile(srcPath);
    Bitmap bitmap = BitmapUtil.rotateBitmap(srcBt, degree);
    PhotoHelper.saveBitmapFile(bitmap, new File(srcPath));
  }

  public int readPictureDegree(String path) {
    int degree = 0;
    try {
      ExifInterface exifInterface = new ExifInterface(path);
      int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
      switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
          degree = 90;
          break;
        case ExifInterface.ORIENTATION_ROTATE_180:
          degree = 180;
          break;
        case ExifInterface.ORIENTATION_ROTATE_270:
          degree = 270;
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return degree;
  }


}
