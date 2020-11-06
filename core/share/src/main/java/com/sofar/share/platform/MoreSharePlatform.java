package com.sofar.share.platform;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.sofar.share.ShareObject;
import com.sofar.share.SharePlatformId;

/**
 * 更多分享，调用系统分享
 */
public class MoreSharePlatform implements SharePlatform {

  @Override
  public void share(@NonNull Context context, @NonNull ShareObject shareObject) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType(shareObject.type);
    String text = shareObject.text;
    if (!TextUtils.isEmpty(shareObject.url)) {
      text += shareObject.url;
    }
    intent.putExtra(Intent.EXTRA_TEXT, text);
    intent.putExtra(Intent.EXTRA_SUBJECT, shareObject.subject);
    if (shareObject.uri != null) {
      intent.putExtra(Intent.EXTRA_STREAM, shareObject.uri);
    }
    context.startActivity(Intent.createChooser(intent, "分享"));
  }

  @NonNull
  @Override
  public String getId() {
    return SharePlatformId.MORE;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public int getIcon() {
    return 0;
  }

  @NonNull
  @Override
  public String getName() {
    return "更多";
  }
}
