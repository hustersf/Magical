package com.sofar.base.widget.banner;


import android.os.Handler;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class BannerAutoHelper {

  private ViewPager2 viewPager2;
  private BannerIndicator indicator;
  private int scrollInterval = 3000;
  private int count;
  int scrollState = ViewPager2.SCROLL_STATE_IDLE;

  Handler handler = new Handler();
  Runnable scrollRunnable = new Runnable() {
    @Override
    public void run() {
      int current = viewPager2.getCurrentItem();
      viewPager2.setCurrentItem(current + 1);
    }
  };

  View.OnAttachStateChangeListener mAttachStateChangeListener =
    new View.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(View v) {
        start();
      }

      @Override
      public void onViewDetachedFromWindow(View v) {
        stop();
      }
    };

  ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageScrollStateChanged(int state) {
      super.onPageScrollStateChanged(state);
      scrollState = state;
      updateScrollState();
    }
  };

  public BannerAutoHelper(@NonNull ViewPager2 viewPager2, @NonNull BannerIndicator indicator) {
    this.viewPager2 = viewPager2;
    this.indicator = indicator;
    viewPager2.addOnAttachStateChangeListener(mAttachStateChangeListener);
    viewPager2.registerOnPageChangeCallback(mOnPageChangeCallback);
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void setScrollInterval(int interval) {
    this.scrollInterval = interval;
  }

  public void start() {
    updateScrollState();
  }

  public void stop() {
    handler.removeCallbacks(scrollRunnable);
  }

  private void updateScrollState() {
    if (scrollState != ViewPager2.SCROLL_STATE_IDLE || count <= 1) {
      handler.removeCallbacks(scrollRunnable);
    } else {
      handler.removeCallbacks(scrollRunnable);
      handler.postDelayed(scrollRunnable, scrollInterval);
    }

    if (scrollState == RecyclerView.SCROLL_STATE_IDLE && count > 1) {
      int position = viewPager2.getCurrentItem();
      indicator.setIndicator(position % count);
    }
  }

  public void destroy() {
    stop();
    viewPager2.removeOnAttachStateChangeListener(mAttachStateChangeListener);
    viewPager2.unregisterOnPageChangeCallback(mOnPageChangeCallback);
  }

}
