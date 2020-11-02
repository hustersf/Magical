package com.sofar.apollo.home.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.base.blur.BlurUtil;
import com.sofar.base.util.AssetUtil;
import com.sofar.base.viewbinder.ViewBinder;

public class HomeViewBinder extends ViewBinder<HomeContext> {

  ViewGroup homeRoot;
  Bitmap homeImg;


  @Override
  protected void onCreate() {
    super.onCreate();
    homeRoot = view.findViewById(R.id.home_root);
  }

  @Override
  protected void onBind(HomeContext data) {
    super.onBind(data);
    homeImg = AssetUtil.getImageFromAssetsFile(context, "img/home_img.jpg");
    homeRoot.setBackground(new BitmapDrawable(context.getResources(), homeImg));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (homeImg != null) {
      homeImg.recycle();
    }
  }
}
