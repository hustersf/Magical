package com.sofar.snapu.viewbinder;


import android.widget.ImageView;

import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;
import com.sofar.snapu.R;
import com.sofar.snapu.model.Banner;

public class BannerItemViewBinder extends RecyclerViewBinder<Banner> {

  ImageView photoView;

  @Override
  protected void onCreate() {
    super.onCreate();
    photoView = bindView(R.id.photo);
  }


  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    ImageExtKt.loadImage(photoView, data.imgUrl);
  }
}
