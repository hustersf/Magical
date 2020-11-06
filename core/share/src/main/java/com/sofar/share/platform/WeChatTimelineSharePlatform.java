package com.sofar.share.platform;

import androidx.annotation.NonNull;

/**
 * 微信朋友圈
 */
public class WeChatTimelineSharePlatform extends WeChatSharePlatform {

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
