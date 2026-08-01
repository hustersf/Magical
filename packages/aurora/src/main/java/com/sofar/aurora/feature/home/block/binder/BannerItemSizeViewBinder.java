package com.sofar.aurora.feature.home.block.binder;

import android.view.ViewGroup;

import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.core.ui.util.DimensExtKt;
import com.sofar.core.ui.util.WindowExtKt;

public class BannerItemSizeViewBinder extends RecyclerViewBinder<Banner> {

  @Override
  protected void onCreate() {
    super.onCreate();
    ViewGroup.LayoutParams lp = view.getLayoutParams();
    lp.width = WindowExtKt.getScreenWidth(context) - DimensExtKt.dp2pxInt(context, 2 * 15);
    view.setLayoutParams(lp);
  }

}
