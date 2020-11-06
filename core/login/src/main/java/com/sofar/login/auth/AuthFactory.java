package com.sofar.login.auth;

import android.content.Context;

import androidx.annotation.NonNull;

public class AuthFactory {

  public static AuthClient buildAuthClient(@NonNull Context context, @AuthClientId String id) {
    switch (id) {
      case AuthClientId.QQ:
        return new QQAuth(context);
    }
    return new EmptyAuth(context);
  }

}
