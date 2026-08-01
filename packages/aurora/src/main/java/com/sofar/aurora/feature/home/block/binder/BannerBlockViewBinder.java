package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.BannerAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.core.ui.banner.BannerIndicator;
import com.sofar.core.ui.recyclerview.CenterPagerSnapHelper;
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration;
import com.sofar.core.ui.util.DimensExtKt;

public class BannerBlockViewBinder extends RecyclerViewBinder<HomeBlock<Banner>> {

  @NonNull
  RecyclerView mRecyclerView;
  @NonNull
  BannerAdapter mAdapter;
  @NonNull
  LinearLayoutManager layoutManager;
  @NonNull
  BannerIndicator mIndicatorView;

  int loopCount = 10000;
  int bannerSize;
  int sideSpace;

  @Override
  protected void onCreate() {
    super.onCreate();
    mRecyclerView = bindView(R.id.banner_recycler);
    mIndicatorView = bindView(R.id.banner_indicator);
    mAdapter = new BannerAdapter(loopCount);
    layoutManager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
    int betweenSpace = DimensExtKt.dp2pxInt(context, 30);
    sideSpace = DimensExtKt.dp2pxInt(context, 15);
    LinearMarginItemDecoration itemDecoration =
      new LinearMarginItemDecoration(RecyclerView.HORIZONTAL, sideSpace, betweenSpace);
    mRecyclerView.addItemDecoration(itemDecoration);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(mAdapter);

    CenterPagerSnapHelper snapHelper = new CenterPagerSnapHelper();
    snapHelper.attachToRecyclerView(mRecyclerView);
  }


  @Override
  protected void onBind(HomeBlock<Banner> data) {
    super.onBind(data);
    if (data.results != null && !data.results.isEmpty()) {
      bannerSize = data.results.size();
      mAdapter.setList(data.results);
      mAdapter.notifyDataSetChanged();
      int size = DimensExtKt.dp2pxInt(context, 5);
      int margin = DimensExtKt.dp2pxInt(context, 2);
      mIndicatorView.initIndicatorItems(bannerSize, size, size, margin, margin);

      mRecyclerView.post(() -> {
        layoutManager.scrollToPositionWithOffset(bannerSize * loopCount / 2, -sideSpace);
        mIndicatorView.setIndicator(0);
      });
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mRecyclerView.setAdapter(null);
  }

}
