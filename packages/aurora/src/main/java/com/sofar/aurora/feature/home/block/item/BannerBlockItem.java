package com.sofar.aurora.feature.home.block.item;

import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.BannerAutoScrollViewBinder;
import com.sofar.aurora.feature.home.block.binder.BannerBlockViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class BannerBlockItem extends BlockItem {

  @Override
  public View createView(ViewGroup parent) {
    return ViewUtil.inflate(parent, R.layout.block_item_banner);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new BannerBlockViewBinder());
    viewBinder.addViewBinder(new BannerAutoScrollViewBinder());
    return viewBinder;
  }

}
