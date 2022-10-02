package com.sofar.base.widget.banner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

/**
 * Banner的指示器
 */
public class BannerIndicator extends LinearLayout {

  private int count;
  private int selectColor = 0xFFFFFFFF;
  private int unselectColor = 0x99FFFFFF;
  private int tintColor = 0xFFFFFFFF;

  private Drawable selectedDrawable;
  private Drawable unSelectedDrawable;

  public BannerIndicator(Context context) {
    this(context, null);
  }

  public BannerIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    selectedDrawable = createDrawable(selectColor);
    unSelectedDrawable = createDrawable(unselectColor);
  }

  private GradientDrawable createDrawable(int color) {
    GradientDrawable drawable = new GradientDrawable();
    drawable.setShape(GradientDrawable.OVAL);
    drawable.setColor(color);
    return drawable;
  }

  public void initIndicatorItems(int itemsNumber, int width, int height, int leftMargin,
    int rightMargin) {
    count = itemsNumber;
    removeAllViews();
    for (int i = 0; i < itemsNumber; i++) {
      ImageView imageView = new ImageView(getContext());
      LayoutParams lp = new LayoutParams(width, height);
      lp.leftMargin = leftMargin;
      lp.rightMargin = rightMargin;
      imageView.setLayoutParams(lp);
      imageView.setImageDrawable(unSelectedDrawable);
      imageView.setColorFilter(tintColor);
      addView(imageView);
    }
  }

  public void setIndicator(int position) {
    ImageView imageView;
    for (int i = 0; i < count; i++) {
      imageView = (ImageView) getChildAt(i);
      if (i == position) {
        imageView.setImageDrawable(selectedDrawable);
      } else {
        imageView.setImageDrawable(unSelectedDrawable);
      }
    }
  }

  public void setTintColor(int color) {
    tintColor = color;
  }

  public void setSelectedDrawable(@DrawableRes int resId) {
    this.selectedDrawable = ContextCompat.getDrawable(getContext(), resId);
  }

  public void setUnSelectedDrawable(@DrawableRes int resId) {
    this.unSelectedDrawable = ContextCompat.getDrawable(getContext(), resId);
  }
}
