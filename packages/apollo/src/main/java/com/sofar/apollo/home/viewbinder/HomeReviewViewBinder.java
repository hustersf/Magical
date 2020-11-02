package com.sofar.apollo.home.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.base.blur.BlurUtil;
import com.sofar.base.viewbinder.ViewBinder;

public class HomeReviewViewBinder extends ViewBinder<HomeContext> {

  ViewGroup homeRoot;
  ViewGroup reviewLayout;

  @Override
  protected void onCreate() {
    super.onCreate();
    homeRoot = view.findViewById(R.id.home_root);
    reviewLayout = view.findViewById(R.id.review_layout);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    reviewLayout.post(() -> {
      Bitmap targetBitmap = BlurUtil.getTargetBitmap(homeRoot, reviewLayout);
      reviewLayout.setBackground(new BitmapDrawable(context.getResources(), BlurUtil.getBlurBitmap(context, targetBitmap, 25)));
    });
  }
}
