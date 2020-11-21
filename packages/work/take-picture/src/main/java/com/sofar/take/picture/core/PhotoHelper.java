package com.sofar.take.picture.core;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.sofar.utility.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片拍摄上传帮转类
 */
public class PhotoHelper {

  public static final String TAG = "PhotoHelper";
  public static final String PHOTO_DIR = "photo/source";
  public static final String PHOTO_THUMB_DIR = "photo/thumb";

  public static final String IMAGE_FORMAT = ".jpeg";

  @NonNull
  public Activity activity;
  public long taskId;

  public PhotoHelper(@NonNull Activity activity, long taskId) {
    this.activity = activity;
    this.taskId = taskId;
  }

  public String getPhotoName(long taskId, int order) {
    return taskId + "_" + order;
  }

  /**
   * 将图片保存至本地
   */
  public static void saveBitmapFile(Bitmap bitmap, File dstFile) {
    try {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile));
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
      bos.flush();
      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
      Log.d(TAG, "图片保存失败:" + e.toString());
    }
  }

  /**
   * 获取拍摄图片路径
   */
  public String getPhotoDir() {
    File dir = new File(FileUtil.getFileDir(activity), PHOTO_DIR + "/" + taskId);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.getAbsolutePath();
  }

  /**
   * 获取拍摄图片的缩略图路径
   */
  public String getPhotoThumbDir() {
    File dir = new File(FileUtil.getFileDir(activity), PHOTO_THUMB_DIR + "/" + taskId);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.getAbsolutePath();
  }

  public Uri getUriForFile(File file) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
    } else {
      return Uri.fromFile(file);
    }
  }
}
