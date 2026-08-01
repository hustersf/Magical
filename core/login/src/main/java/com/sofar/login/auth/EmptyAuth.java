package com.sofar.login.auth;

import android.content.Context;
import androidx.annotation.NonNull;

public class EmptyAuth extends AuthClient {

  public EmptyAuth(@NonNull Context context) {
    super(context);
  }

  @Override
  public boolean isAvailable() {
    return false;
  }
}
