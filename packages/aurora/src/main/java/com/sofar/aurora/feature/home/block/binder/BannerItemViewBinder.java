package com.sofar.aurora.feature.home.block.binder;

import android.widget.ImageView;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;

public class BannerItemViewBinder extends RecyclerViewBinder<Banner> {

  @NonNull
  ImageView coverView;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverView = bindView(R.id.cover);
  }

  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    ImageExtKt.loadImage(coverView, data.imageUrl);
  }

}
