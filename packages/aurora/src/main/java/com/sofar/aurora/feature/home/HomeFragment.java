package com.sofar.aurora.feature.home;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.feature.home.model.HomePageList;
import com.sofar.base.page.PageList;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.recycler.RecyclerFragment;

public class HomeFragment extends RecyclerFragment<HomeBlock> {

  @Override
  protected RecyclerAdapter<HomeBlock> onCreateAdapter() {
    return new HomeAdapter();
  }

  @Override
  protected PageList<?, HomeBlock> onCreatePageList() {
    return new HomePageList();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.home_fragment;
  }
}
