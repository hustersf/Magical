package com.sofar.share.platform;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sofar.share.ShareObject;

public abstract class WeChatSharePlatform implements SharePlatform {

  @Override
  public void share(@NonNull Context context, @NonNull ShareObject shareObject) {
  }
}
