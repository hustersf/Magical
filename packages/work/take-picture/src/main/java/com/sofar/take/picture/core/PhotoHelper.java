package com.sofar.take.picture.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.sofar.base.BaseActivity;
import com.sofar.base.callback.ActivityCallback;
import com.sofar.utility.BitmapUtil;
import com.sofar.utility.DateUtil;
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

  /**
   * 缩略图片最大字节数
   */
  private static final int THUMB_MAX_SIZE = 100; //kb

  /**
   * 开启相机拍摄
   */
  public static void startCamera(@NonNull BaseActivity activity) {
    String photoName = DateUtil.getCurrentTimeInString() + IMAGE_FORMAT;
    File file = new File(getPhotoDir(activity), photoName);
    Uri imageUri = getUriForFile(activity, file);
    Intent intent = new Intent();
    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
    activity.startActivityForResult(intent, new ActivityCallback() {
      @Override
      public void onResult(Intent data) {
        Log.d(TAG, "拍摄成功");
        buildThumbFile(activity, file);
      }

      @Override
      public void onCancel(Intent data) {
        Log.d(TAG, "拍摄取消");
      }
    });
  }

  /**
   * 原图生成一张对应的缩略图
   *
   * @param imageFile 原图文件
   */
  public static void buildThumbFile(@NonNull Activity activity, File imageFile) {
    Bitmap srcBt = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
    Bitmap thumbBt = BitmapUtil.compressBitmap(srcBt, THUMB_MAX_SIZE);
    Log.d(TAG, "生成缩图图");

    File thumbFile = new File(getPhotoThumbDir(activity), imageFile.getName());
    saveBitmapFile(thumbBt, thumbFile);
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
  public static String getPhotoDir(@NonNull Context context) {
    File dir = new File(FileUtil.getFileDir(context), PHOTO_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.getAbsolutePath();
  }

  /**
   * 获取拍摄图片的缩略图路径
   */
  public static String getPhotoThumbDir(@NonNull Context context) {
    File dir = new File(FileUtil.getFileDir(context), PHOTO_THUMB_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.getAbsolutePath();
  }

  public static Uri getUriForFile(@NonNull Activity activity, File file) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
    } else {
      return Uri.fromFile(file);
    }
  }
}
