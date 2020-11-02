package com.sofar.apollo.home.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.apollo.learn.LearnActivity;
import com.sofar.base.blur.BlurUtil;
import com.sofar.base.viewbinder.ViewBinder;

public class HomeLearnViewBinder extends ViewBinder<HomeContext> {

  ViewGroup homeRoot;
  ViewGroup learnLayout;

  @Override
  protected void onCreate() {
    super.onCreate();
    homeRoot = view.findViewById(R.id.home_root);
    learnLayout = view.findViewById(R.id.learn_layout);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    learnLayout.post(() -> {
      Bitmap targetBitmap = BlurUtil.getTargetBitmap(homeRoot, learnLayout);
      learnLayout.setBackground(new BitmapDrawable(context.getResources(), BlurUtil.getBlurBitmap(context, targetBitmap, 25)));
    });

    learnLayout.setOnClickListener(v -> {
      if (getActivity() != null) {
        LearnActivity.launch(getActivity());
      }
    });
  }
}
