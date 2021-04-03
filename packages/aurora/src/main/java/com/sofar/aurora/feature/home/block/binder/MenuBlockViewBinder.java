package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.MenuAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Menu;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.CollectionUtil;

public class MenuBlockViewBinder extends RecyclerViewBinder<HomeBlock<Menu>> {

  @NonNull
  RecyclerView mRecyclerView;
  @NonNull
  MenuAdapter mAdapter;
  @NonNull
  GridLayoutManager mLayoutManager;

  @Override
  protected void onCreate() {
    super.onCreate();
    mRecyclerView = bindView(R.id.menu_recycler);
    mAdapter = new MenuAdapter();
    mLayoutManager = new GridLayoutManager(context, 3, RecyclerView.VERTICAL, false);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onBind(HomeBlock<Menu> data) {
    super.onBind(data);
    if (!CollectionUtil.isEmpty(data.results)) {
      mLayoutManager.setSpanCount(data.results.size());
      mAdapter.setList(data.results);
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mRecyclerView.setAdapter(null);
  }
}
