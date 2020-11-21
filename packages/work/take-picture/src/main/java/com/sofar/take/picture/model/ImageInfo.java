package com.sofar.take.picture.model;

import android.content.Context;

import java.io.Serializable;
import com.sofar.take.picture.core.PhotoHelper;

/**
 * 图片信息
 * {@link PhotoHelper#getPhotoDir(Context)}
 * {@link PhotoHelper#getPhotoThumbDir(Context)}
 *
 * 图片名字+上述目录可唯一确定图片路径
 */
public class ImageInfo implements Serializable {

  /**
   * 图片名字
   */
  public String name;

  /**
   * 图片宽度
   */
  public int width;

  /**
   * 图片高度
   */
  public int height;

}
