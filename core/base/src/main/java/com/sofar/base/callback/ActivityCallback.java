package com.sofar.base.callback;

import android.content.Intent;

/**
 * Activity回调
 */
public interface ActivityCallback {

  void onResult(Intent data);

  void onCancel(Intent data);
}
