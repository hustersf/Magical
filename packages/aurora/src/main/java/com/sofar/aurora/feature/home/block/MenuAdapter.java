package com.sofar.aurora.feature.home.block;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.MenuClickViewBinder;
import com.sofar.aurora.feature.home.block.binder.MenuItemViewBinder;
import com.sofar.aurora.model.Menu;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class MenuAdapter extends RecyclerAdapter<Menu> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.block_item_menu_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Menu> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new MenuItemViewBinder());
    viewBinder.addViewBinder(new MenuClickViewBinder());
    return viewBinder;
  }

}
