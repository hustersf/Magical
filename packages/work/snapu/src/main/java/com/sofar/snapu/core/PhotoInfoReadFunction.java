package com.sofar.snapu.core;

import android.media.ExifInterface;

import com.sofar.snapu.model.ImageInfo;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Function;

/**
 * 获取拍摄图片的 信息
 */
public class PhotoInfoReadFunction implements Function<String, ImageInfo> {

  @Override
  public ImageInfo apply(String imagePath) {
    return readPictureInfo(imagePath);
  }

  /**
   * 读取图片信息
   */
  public ImageInfo readPictureInfo(String path) {
    ImageInfo imageInfo = new ImageInfo();
    imageInfo.name = new File(path).getName();
    try {
      ExifInterface exifInterface = new ExifInterface(path);
      imageInfo.width = Integer.valueOf(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
      imageInfo.height = Integer.valueOf(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInfo;
  }

}
