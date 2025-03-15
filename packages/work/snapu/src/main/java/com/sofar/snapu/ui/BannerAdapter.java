package com.sofar.snapu.ui;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.snapu.R;
import com.sofar.snapu.model.Banner;
import com.sofar.snapu.viewbinder.BannerItemViewBinder;
import com.sofar.utility.ViewUtil;

public class BannerAdapter extends RecyclerAdapter<Banner> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.banner_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Banner> onCreateViewBinder(int viewType) {
    return new BannerItemViewBinder();
  }
}
