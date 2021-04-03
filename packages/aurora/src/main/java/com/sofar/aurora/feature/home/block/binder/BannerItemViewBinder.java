package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;

public class BannerItemViewBinder extends RecyclerViewBinder<Banner> {

  @NonNull
  SofarImageView coverView;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverView = bindView(R.id.cover);
  }

  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    coverView.bindUrl(data.imageUrl);
  }

}
