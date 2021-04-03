package com.sofar.aurora.feature.home.block.item;

import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.MenuBlockViewBinder;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class MenuBlockItem extends BlockItem {

  @Override
  public View createView(ViewGroup parent) {
    return ViewUtil.inflate(parent, R.layout.block_item_menu);
  }

  @Override
  public RecyclerViewBinder createViewBinder() {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new MenuBlockViewBinder());
    return viewBinder;
  }

}
