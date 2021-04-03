package com.sofar.aurora.feature.home;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sofar.aurora.R;
import com.sofar.utility.DeviceUtil;

public class HomeTabItem extends RelativeLayout {

  @NonNull
  ImageView tabIconView;
  @NonNull
  TextView tabTextView;

  int selectorColor;
  int normalColor;

  public HomeTabItem(Context context) {
    super(context);
    init();
  }

  private void init() {
    setGravity(Gravity.CENTER);
    selectorColor = ContextCompat.getColor(getContext(), R.color.home_tab_icon_selected);
    normalColor = ContextCompat.getColor(getContext(), R.color.home_tab_icon_normal);
    addIconView();
    addTextView();
  }

  private void addIconView() {
    tabIconView = new ImageView(getContext());
    tabIconView.setId(R.id.tab_icon);
    tabIconView.setColorFilter(normalColor);
    int iconSize = DeviceUtil.dp2px(getContext(), 24);
    RelativeLayout.LayoutParams iconLp = new RelativeLayout.LayoutParams(iconSize, iconSize);
    iconLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
    addView(tabIconView, iconLp);
  }

  private void addTextView() {
    tabTextView = new TextView(getContext());
    tabTextView.setId(R.id.tab_text);
    tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
    tabTextView.setTextColor(normalColor);
    int size = ViewGroup.LayoutParams.WRAP_CONTENT;
    RelativeLayout.LayoutParams textLp = new RelativeLayout.LayoutParams(size, size);
    textLp.addRule(RelativeLayout.BELOW, R.id.tab_icon);
    textLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
    addView(tabTextView, textLp);
  }

  public void bind(String tabName, @DrawableRes int tabIcon) {
    tabTextView.setText(tabName);
    tabIconView.setImageResource(tabIcon);
  }

  @Override
  public void setSelected(boolean selected) {
    super.setSelected(selected);
    tabIconView.setColorFilter(selected ? selectorColor : normalColor);
    tabTextView.setTextColor(selected ? selectorColor : normalColor);
  }
}
