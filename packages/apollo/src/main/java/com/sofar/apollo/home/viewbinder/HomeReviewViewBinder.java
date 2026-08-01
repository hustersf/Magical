package com.sofar.apollo.home.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.core.common.extension.BitmapExtKt;

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
      Bitmap targetBitmap = BitmapExtKt.getTargetAreaOf(homeRoot, reviewLayout);
      reviewLayout.setBackground(
        new BitmapDrawable(context.getResources(), BitmapExtKt.blur(targetBitmap, context, 25)));
    });
  }
}
