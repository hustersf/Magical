package com.sofar.apollo.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.sofar.apollo.R;

public class HomeTabBar extends LinearLayout {

  private OnTabClickListener listener;

  public HomeTabBar(Context context) {
    this(context, null);
  }

  public HomeTabBar(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HomeTabBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.HORIZONTAL);
    setGravity(Gravity.CENTER_VERTICAL);

    addDefaultItems();
  }

  private void addDefaultItems() {
    LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
    lp.weight = 1;

    HomeItem listenItem = new HomeItem(getContext());
    listenItem.setIcon(R.drawable.listen_tab_icon);
    addView(listenItem, lp);
    listenItem.setOnClickListener(v -> {
      if (listener != null) {
        listener.OnTabClick(0, listenItem);
      }
    });

    HomeItem recordItem = new HomeItem(getContext());
    recordItem.setIcon(R.drawable.chart_tab_icon);
    addView(recordItem, lp);
    recordItem.setOnClickListener(v -> {
      if (listener != null) {
        listener.OnTabClick(1, recordItem);
      }
    });
  }

  public void setOnTabClickListener(OnTabClickListener listener) {
    this.listener = listener;
  }

  public interface OnTabClickListener {
    void OnTabClick(int index, View view);
  }

}
