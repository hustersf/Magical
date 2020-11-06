package com.sofar.share.platform;

import androidx.annotation.NonNull;

/**
 * QQ好友
 */
public class QQFriendSharePlatform extends QQSharePlatform {

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
    return 0;
  }

  @NonNull
  @Override
  public String getName() {
    return null;
  }
}
