package com.sofar.aurora.feature.home.block;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.BannerClickViewBinder;
import com.sofar.aurora.feature.home.block.binder.BannerItemSizeViewBinder;
import com.sofar.aurora.feature.home.block.binder.BannerItemViewBinder;
import com.sofar.aurora.model.Banner;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class BannerAdapter extends RecyclerAdapter<Banner> {

  int loopCount;

  public BannerAdapter(int loopCount) {
    this.loopCount = loopCount;
  }

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.block_item_banner_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Banner> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new BannerItemViewBinder());
    viewBinder.addViewBinder(new BannerItemSizeViewBinder());
    viewBinder.addViewBinder(new BannerClickViewBinder());
    return viewBinder;
  }

  @Override
  public int getItemCount() {
    return super.getItemCount() > 1 ? super.getItemCount() * loopCount : super.getItemCount();
  }

  @Nullable
  @Override
  public Banner getItem(int position) {
    return super.getItemCount() == 0 ? null : super.getItem(position % super.getItemCount());
  }


}
