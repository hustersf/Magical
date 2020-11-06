package com.sofar.login.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import io.reactivex.Observable;

public class EmptyAuth extends AuthClient {

  public EmptyAuth(@NonNull Context context) {
    super(context);
  }

  @Override
  public Observable requestAuth() {
    return Observable.error(new Throwable("empty auth"));
  }

  @Override
  public boolean isAvailable() {
    return false;
  }
}
