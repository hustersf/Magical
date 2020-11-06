package com.sofar.apollo.home.viewbinder;

import com.sofar.apollo.R;
import com.sofar.apollo.SofarApp;
import com.sofar.apollo.mine.MineActivity;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.image.widget.SofarImageView;

public class HomeAuthorViewBinder extends ViewBinder<HomeContext> {

  SofarImageView author;

  @Override
  protected void onCreate() {
    super.onCreate();
    author = bindView(R.id.author);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    author.bindUrl(SofarApp.ME.headUrl);
    author.setOnClickListener(v -> {
      if (getActivity() != null) {
        MineActivity.launch(getActivity());
      }
    });
  }
}
