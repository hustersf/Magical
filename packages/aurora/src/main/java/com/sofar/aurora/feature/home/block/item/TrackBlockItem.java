package com.sofar.aurora.feature.home.block.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.BlockMoreViewBinder;
import com.sofar.aurora.feature.home.block.binder.BlockNameViewBinder;
import com.sofar.aurora.feature.home.block.binder.TrackBlockViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class TrackBlockItem extends BlockItem {
  @Override
  public View createView(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_track, parent, false);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new TrackBlockViewBinder());
    viewBinder.addViewBinder(new BlockNameViewBinder());
    viewBinder.addViewBinder(new BlockMoreViewBinder());
    return viewBinder;
  }
}
