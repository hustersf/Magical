package com.sofar.apollo.mine.viewbinder;

import android.content.Intent;
import android.view.View;

import com.sofar.apollo.R;
import com.sofar.apollo.SofarApp;
import com.sofar.apollo.splash.SplashActivity;
import com.sofar.base.app.AppLifeManager;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.login.Account;
import com.sofar.login.model.User;

public class LogoutViewBinder extends ViewBinder<User> {

  View logoutView;

  @Override
  protected void onCreate() {
    super.onCreate();
    logoutView = bindView(R.id.logout);
    logoutView.setOnClickListener(v -> {
      logout();
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private void logout() {
    Account.logout(context);
    SofarApp.ME.clear();
    AppLifeManager.get().finishAllActivity();

    Intent intent = new Intent(context, SplashActivity.class);
    context.startActivity(intent);
  }
}
