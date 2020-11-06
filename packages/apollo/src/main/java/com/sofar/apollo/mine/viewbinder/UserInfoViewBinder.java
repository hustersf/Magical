package com.sofar.apollo.mine.viewbinder;

import android.widget.TextView;

import com.sofar.apollo.R;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.login.model.User;

public class UserInfoViewBinder extends ViewBinder<User> {

  SofarImageView author;
  TextView name;

  @Override
  protected void onCreate() {
    super.onCreate();
    author = bindView(R.id.author);
    name = bindView(R.id.name);
  }

  @Override
  protected void onBind(User data) {
    super.onBind(data);
    author.bindUrl(data.headUrl);
    name.setText(data.name);
  }
}
