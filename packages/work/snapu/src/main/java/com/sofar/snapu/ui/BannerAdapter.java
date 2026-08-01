package com.sofar.snapu.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.snapu.R;
import com.sofar.snapu.model.Banner;
import com.sofar.snapu.viewbinder.BannerItemViewBinder;

public class BannerAdapter extends RecyclerAdapter<Banner> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item, parent, false);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Banner> onCreateViewBinder(int viewType) {
    return new BannerItemViewBinder();
  }
}
