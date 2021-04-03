package com.sofar.aurora.feature.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class HomeTabBar extends LinearLayout implements View.OnClickListener {

  private static final String TAG = "HomeTabBar";

  List<HomeTabItem> mHomeTabItems = new ArrayList<>();
  private OnTabBarClickListener mListener;

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
    setGravity(Gravity.CENTER);
  }

  public void addHomeTab(int tabId, String tabName, @DrawableRes int tabIcon) {
    LinearLayout.LayoutParams lp =
      new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
    lp.weight = 1;
    HomeTabItem item = new HomeTabItem(getContext());
    item.setOnClickListener(this);
    item.bind(tabName, tabIcon);
    addView(item, lp);
    mHomeTabItems.add(item);
  }

  public void setTabSelected(int index) {
    if (index < 0 || index > getChildCount() - 1) {
      return;
    }
    for (int i = 0; i < getChildCount(); i++) {
      if (index == i) {
        getChildAt(i).setSelected(true);
      } else {
        getChildAt(i).setSelected(false);
      }
    }
  }

  @Override
  public void onClick(View v) {
    int index = mHomeTabItems.indexOf(v);

    for (HomeTabItem item : mHomeTabItems) {
      item.setSelected(v == item);
    }

    if (mListener != null) {
      mListener.onTabBarClick(index, v);
    }
  }

  public interface OnTabBarClickListener {
    void onTabBarClick(int index, View view);
  }

  public void setOnTabBarClickListener(OnTabBarClickListener listener) {
    mListener = listener;
  }
}
