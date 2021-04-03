package com.sofar.aurora.feature.home.block.item;

import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.ArtistBlockViewBinder;
import com.sofar.aurora.feature.home.block.binder.BlockMoreViewBinder;
import com.sofar.aurora.feature.home.block.binder.BlockNameViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class ArtistBlockItem extends BlockItem {

  @Override
  public View createView(ViewGroup parent) {
    return ViewUtil.inflate(parent, R.layout.block_item_artist);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new ArtistBlockViewBinder());
    viewBinder.addViewBinder(new BlockNameViewBinder());
    viewBinder.addViewBinder(new BlockMoreViewBinder());
    return viewBinder;
  }
}
