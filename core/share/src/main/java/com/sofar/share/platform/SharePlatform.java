package com.sofar.share.platform;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.sofar.share.ShareObject;
import com.sofar.share.SharePlatformId;

public interface SharePlatform {

  String TAG = "SharePlatform";

  /**
   * 分享行为
   */
  void share(@NonNull Context context, @NonNull ShareObject shareObject);

  /**
   * 分享平台唯一id
   */
  @SharePlatformId
  @NonNull
  String getId();

  /**
   * 是否可用
   */
  boolean isAvailable();

  /**
   * 分享平台图标
   */
  @DrawableRes
  int getIcon();

  /**
   * 分享平台名字
   */
  @NonNull
  String getName();

}
