package com.sofar.base.widget.banner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;

import com.sofar.base.R;

/**
 * Banner的指示器
 */
public class BannerIndicator extends LinearLayout {

  private int count;

  public BannerIndicator(Context context) {
    this(context, null);
  }

  public BannerIndicator(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  private int selectedDrawable = R.drawable.banner_indicator_selected;
  private int unSelectedDrawable = R.drawable.banner_indicator_unselected;

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
      imageView.setImageResource(unSelectedDrawable);
      addView(imageView);
    }
  }

  public void setIndicator(int position) {
    ImageView imageView;
    for (int i = 0; i < count; i++) {
      imageView = (ImageView) getChildAt(i);
      if (i == position) {
        imageView.setImageResource(selectedDrawable);
      } else {
        imageView.setImageResource(unSelectedDrawable);
      }
    }
  }

  public void setSelectedDrawable(@DrawableRes int selectedDrawable) {
    this.selectedDrawable = selectedDrawable;
  }

  public void setUnSelectedDrawable(@DrawableRes int unSelectedDrawable) {
    this.unSelectedDrawable = unSelectedDrawable;
  }
}
