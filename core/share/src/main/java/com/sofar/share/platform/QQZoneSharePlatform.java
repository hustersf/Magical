package com.sofar.share.platform;

import androidx.annotation.NonNull;

import com.sofar.share.R;

/**
 * QQ空间
 */
public class QQZoneSharePlatform extends QQSharePlatform {

  @NonNull
  @Override
  public String getId() {
    return null;
  }

  @Override
  public boolean isAvailable() {
    return false;
  }

  @Override
  public int getIcon() {
    return R.drawable.share_icon_qqzone;
  }

  @NonNull
  @Override
  public String getName() {
    return null;
  }
}
