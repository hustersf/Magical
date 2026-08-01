package com.sofar.aurora.feature.home.block.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sofar.aurora.R;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public abstract class BlockItem {

  public abstract View createView(ViewGroup parent);

  public abstract RecyclerViewBinder createViewBinder();

  public static BlockItem DEFAULT = new BlockItem() {

    @Override
    public View createView(ViewGroup parent) {
      return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_un_support, parent, false);
    }

    @Override
    public RecyclerViewBinder createViewBinder() {
      return new RecyclerViewBinder();
    }
  };

}
