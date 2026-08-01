package com.sofar.aurora.feature.home.block.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.BannerAutoScrollViewBinder;
import com.sofar.aurora.feature.home.block.binder.BannerBlockViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BannerBlockItem extends BlockItem {

  @Override
  public View createView(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_banner, parent, false);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new BannerBlockViewBinder());
    viewBinder.addViewBinder(new BannerAutoScrollViewBinder());
    return viewBinder;
  }

}
