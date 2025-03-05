package com.sofar.apollo.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.sofar.apollo.R;
import com.sofar.utility.DeviceUtil;

public class HomeItem extends LinearLayout {

  @NonNull
  ImageView imageView;
  @NonNull
  TextView textView;

  public HomeItem(Context context) {
    this(context, null);
  }

  public HomeItem(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HomeItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);
    setGravity(Gravity.CENTER);

    initIcon();
    initText();
  }

  private void initIcon() {
    imageView = new ImageView(getContext());
    int width = DeviceUtil.dp2px(getContext(), 25);
    int height = DeviceUtil.dp2px(getContext(), 25);
    ViewGroup.LayoutParams imgLp = new ViewGroup.LayoutParams(width, height);
    addView(imageView, imgLp);
    imageView.setVisibility(GONE);
  }

  private void initText() {
    textView = new TextView(getContext());
    textView.setTextColor(
      getContext().getResources().getColor(com.sofar.base.R.color.main_text_color));
    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
    int width = ViewGroup.LayoutParams.WRAP_CONTENT;
    int height = ViewGroup.LayoutParams.WRAP_CONTENT;
    ViewGroup.MarginLayoutParams textLp = new ViewGroup.MarginLayoutParams(width, height);
    textLp.topMargin = DeviceUtil.dp2px(getContext(), 5);
    addView(textView, textLp);
    textView.setVisibility(GONE);
  }

  public void setIcon(@DrawableRes int drawableId) {
    imageView.setImageResource(drawableId);
    imageView.setVisibility(VISIBLE);
  }

  public void setIcon(Drawable drawable) {
    imageView.setImageDrawable(drawable);
    imageView.setVisibility(VISIBLE);
  }

  public void setText(@StringRes int id) {
    textView.setText(id);
    textView.setVisibility(VISIBLE);
  }

  public void setText(CharSequence text) {
    textView.setText(text);
    textView.setVisibility(VISIBLE);
  }

  public void setTextColor(int color) {
    textView.setTextColor(color);
  }

}
