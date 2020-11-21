package com.sofar.take.picture.model;

import android.app.Activity;
import android.media.ExifInterface;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sofar.base.page.retrofit.SofarRetrofitPageList;
import com.sofar.take.picture.core.PhotoHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PhotoPageList extends SofarRetrofitPageList<PhotoPageResponse, ImageInfo> {

  @NonNull
  Activity activity;

  public PhotoPageList(@NonNull Activity activity) {
    this.activity = activity;
  }

  @Override
  protected Observable<PhotoPageResponse> onCreateRequest() {
    return Observable.fromCallable(() -> {
      PhotoPageResponse response = new PhotoPageResponse();
      List<ImageInfo> list = new ArrayList<>();
      response.items = list;

      String path = PhotoHelper.getPhotoThumbDir(activity);
      File dir = new File(path);
      for (File file : dir.listFiles()) {
        list.add(readPictureInfo(file));
      }

      Log.d("PhotoPageList", "thread=" + Thread.currentThread().getName());
      return response;
    }).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread());
  }

  /**
   * 读取图片信息
   */
  private ImageInfo readPictureInfo(File file) {
    ImageInfo imageInfo = new ImageInfo();
    imageInfo.name = file.getName();
    try {
      ExifInterface exifInterface = new ExifInterface(file.getPath());
      imageInfo.width = Integer.valueOf(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
      imageInfo.height = Integer.valueOf(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInfo;
  }
}
