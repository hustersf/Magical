package com.sofar.snapu.viewbinder;


import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.snapu.R;
import com.sofar.snapu.model.Banner;

public class BannerItemViewBinder extends RecyclerViewBinder<Banner> {

  SofarImageView photoView;

  @Override
  protected void onCreate() {
    super.onCreate();
    photoView = bindView(R.id.photo);
  }


  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    photoView.bindUrl(data.imgUrl);
  }
}
