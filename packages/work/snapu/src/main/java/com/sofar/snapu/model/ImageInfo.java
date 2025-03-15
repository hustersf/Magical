package com.sofar.snapu.model;

import java.io.Serializable;

import com.sofar.snapu.core.PhotoHelper;

/**
 * 图片信息
 * {@link PhotoHelper#getPhotoDir()}
 * {@link PhotoHelper#getPhotoThumbDir()}
 * <p>
 * 图片名字+上述目录可唯一确定图片路径
 */
public class ImageInfo implements Serializable {

  /**
   * 图片生成时所属于的任务id
   */
  public long taskId;

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

  /**
   * 图片地址
   */
  public String url;

}
