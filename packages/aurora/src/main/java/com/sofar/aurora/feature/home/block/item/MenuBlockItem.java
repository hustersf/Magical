package com.sofar.aurora.feature.home.block.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.MenuBlockViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class MenuBlockItem extends BlockItem {

  @Override
  public View createView(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_menu, parent, false);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new MenuBlockViewBinder());
    return viewBinder;
  }

}
