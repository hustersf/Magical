package com.sofar.aurora.feature.home.block.binder;

import android.view.View;

import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BannerClickViewBinder extends RecyclerViewBinder<Banner> {

  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

      }
    });
  }
}
