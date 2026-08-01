package com.sofar.login.auth;

import android.content.Context;
import androidx.annotation.NonNull;

public abstract class AuthClient {

  @NonNull
  protected Context context;

  public AuthClient(@NonNull Context context) {
    this.context = context;
  }

  public abstract boolean isAvailable();

}
