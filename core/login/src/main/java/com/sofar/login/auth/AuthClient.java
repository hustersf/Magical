package com.sofar.login.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import io.reactivex.Observable;

public abstract class AuthClient {

  @NonNull
  protected Context context;

  public AuthClient(@NonNull Context context) {
    this.context = context;
  }

  public abstract Observable requestAuth();

  public abstract boolean isAvailable();

}
