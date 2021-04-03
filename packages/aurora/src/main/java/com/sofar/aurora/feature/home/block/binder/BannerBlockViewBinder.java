package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.BannerAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.base.widget.BannerIndicator;
import com.sofar.utility.CollectionUtil;
import com.sofar.utility.DeviceUtil;
import com.sofar.widget.recycler.CenterPagerSnapHelper;
import com.sofar.widget.recycler.LinearMarginItemDecoration;

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
    int betweenSpace = DeviceUtil.dp2px(context, 30);
    sideSpace = DeviceUtil.dp2px(context, 15);
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
    if (!CollectionUtil.isEmpty(data.results)) {
      bannerSize = data.results.size();
      mAdapter.setList(data.results);
      mAdapter.notifyDataSetChanged();
      int size = DeviceUtil.dp2px(context, 5);
      int margin = DeviceUtil.dp2px(context, 2);
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
