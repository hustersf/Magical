package com.sofar.apollo.home.viewbinder;

import android.widget.ImageView;

import com.sofar.apollo.R;
import com.sofar.apollo.SofarApp;
import com.sofar.apollo.mine.MineActivity;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.image.ImageExtKt;

public class HomeAuthorViewBinder extends ViewBinder<HomeContext> {

  ImageView author;

  @Override
  protected void onCreate() {
    super.onCreate();
    author = bindView(R.id.author);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    ImageExtKt.loadImage(author, SofarApp.ME.headUrl);
    author.setOnClickListener(v -> {
      if (getActivity() != null) {
        MineActivity.launch(getActivity());
      }
    });
  }
}
