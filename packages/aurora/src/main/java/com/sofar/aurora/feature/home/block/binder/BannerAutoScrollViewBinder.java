package com.sofar.aurora.feature.home.block.binder;

import android.os.Handler;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.base.widget.banner.BannerIndicator;
import com.sofar.utility.CollectionUtil;

public class BannerAutoScrollViewBinder extends RecyclerViewBinder<HomeBlock<Banner>> {

  @NonNull
  RecyclerView mRecyclerView;
  @NonNull
  BannerIndicator mIndicatorView;

  Handler mHandler = new Handler();
  int scrollState = RecyclerView.SCROLL_STATE_IDLE;
  final long SCROLL_INTERVAL = 3000;
  boolean attach = true;
  int bannerSize;

  Runnable scrollRunnable = new Runnable() {
    @Override
    public void run() {
      View current = mRecyclerView.getChildAt(0);
      if (current == null) {
        return;
      }
      int index = mRecyclerView.getChildAdapterPosition(current);

      mRecyclerView.smoothScrollToPosition(index + 1);
    }
  };

  RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      updateScrollState();
    }
  };

  View.OnAttachStateChangeListener mAttachStateChangeListener =
    new View.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View v) {
        attach = true;
        updateScrollState();
      }

      @Override
      public void onViewDetachedFromWindow(View v) {
        attach = false;
        updateScrollState();
      }
    };

  @Override
  protected void onCreate() {
    super.onCreate();
    mRecyclerView = bindView(R.id.banner_recycler);
    mIndicatorView = bindView(R.id.banner_indicator);
    mRecyclerView.addOnAttachStateChangeListener(mAttachStateChangeListener);
    mRecyclerView.addOnScrollListener(mScrollListener);
  }

  @Override
  protected void onBind(HomeBlock<Banner> data) {
    super.onBind(data);
    if (!CollectionUtil.isEmpty(data.results)) {
      bannerSize = data.results.size();
      updateScrollState();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mRecyclerView.removeOnAttachStateChangeListener(mAttachStateChangeListener);
    mRecyclerView.removeOnScrollListener(mScrollListener);
    mHandler.removeCallbacks(scrollRunnable);
  }

  private void updateScrollState() {
    if (!attach) {
      mHandler.removeCallbacks(scrollRunnable);
      return;
    }

    if (scrollState != RecyclerView.SCROLL_STATE_IDLE || bannerSize <= 1) {
      mHandler.removeCallbacks(scrollRunnable);
    } else {
      mHandler.removeCallbacks(scrollRunnable);
      mHandler.postDelayed(scrollRunnable, SCROLL_INTERVAL);
    }

    if (scrollState == RecyclerView.SCROLL_STATE_IDLE && bannerSize > 1) {
      View firstChild = mRecyclerView.getChildAt(0);
      int position = mRecyclerView.getChildAdapterPosition(firstChild);
      if (position == RecyclerView.NO_POSITION) {
        position = 0;
      }
      mIndicatorView.setIndicator(position % bannerSize);
    }
  }

}
