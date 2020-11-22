package com.sofar.take.picture.viewbinder;


import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.take.picture.R;
import com.sofar.take.picture.model.Banner;

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
