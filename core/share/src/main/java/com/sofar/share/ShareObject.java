package com.sofar.share;

import android.net.Uri;

/**
 * 构造分享数据
 */
public class ShareObject {

  /**
   * 分享类型
   * 有的平台会自动识别文件类型，而不是看传递的type类型
   */
  @ShareType
  public String type = ShareType.TEXT;

  /**
   * 分享主文案
   */
  public String text;

  /**
   * 分享副文案
   */
  public String subject;

  /**
   * 分享链接
   */
  public String url;

  /**
   * 分享文件类的uri
   */
  public Uri uri;

  public @interface ShareType {

    String TEXT = "text/plain";

    String IMAGE = "image/*";

    String AUDIO = "audio/*";

    String VIDEO = "video/*";

  }

}
