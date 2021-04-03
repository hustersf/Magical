package com.sofar.widget.flowlayout;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sofar.widget.Util;

/**
 * tag标签布局样例
 * 如果想改变样式，可参考{{@link #refreshView(),#updateDrawable(TextView)}}
 */
public class FlowTagList extends FlowLayout {
  private List<String> mTags;
  private OnTagClickListener mOnTagClickListener;

  private int mColor = 0xFF3DDAE7;
  private int mStrokeWidth;

  public FlowTagList(Context context) {
    this(context, null);
  }

  public FlowTagList(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FlowTagList(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mStrokeWidth = Util.dp2px(context, 1);
  }

  /**
   * 设置tag数据源
   *
   * @param tags
   */
  public void setTags(List<String> tags) {
    mTags = tags;
    refreshView();
  }

  private void refreshView() {
    removeAllViews();
    for (int i = 0; i < mTags.size(); i++) {
      TextView tv = new TextView(getContext());
      ViewGroup.MarginLayoutParams lp =
        new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      int margin = Util.dp2px(getContext(), 5);
      lp.setMargins(margin, margin, margin, margin);
      tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
      int paddingLeft = Util.dp2px(getContext(), 10);
      int paddingTop = Util.dp2px(getContext(), 5);
      tv.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);

      updateDrawable(tv);
      updateTextColor(tv);

      tv.setText(mTags.get(i));
      addView(tv, lp);
      initEvent(tv, i);
    }
  }

  private void updateDrawable(TextView tv) {
    int radius = Util.dp2px(getContext(), 1000);
    GradientDrawable normalDrawable = new GradientDrawable();
    normalDrawable.setColor(Color.WHITE);
    normalDrawable.setStroke(mStrokeWidth, mColor);
    normalDrawable.setCornerRadius(radius);

    GradientDrawable pressedDrawable = new GradientDrawable();
    pressedDrawable.setColor(mColor);
    pressedDrawable.setCornerRadius(radius);

    StateListDrawable stateListDrawable = new StateListDrawable();
    stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, normalDrawable);
    stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
    tv.setBackground(stateListDrawable);
  }

  private void updateTextColor(TextView tv) {
    int[] colors = new int[]{mColor, Color.WHITE};
    int[][] states = new int[2][];
    states[0] = new int[]{-android.R.attr.state_pressed};
    states[1] = new int[]{android.R.attr.state_pressed};
    ColorStateList colorStateList = new ColorStateList(states, colors);
    tv.setTextColor(colorStateList);
  }

  private void initEvent(final TextView tv, final int position) {
    tv.setOnClickListener(v -> {
      if (mOnTagClickListener != null) {
        mOnTagClickListener.OnTagClick(tv.getText().toString(), position);
      }
    });
  }


  /**
   * tag点击监听
   *
   * @param onTagClickListener
   */
  public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
    mOnTagClickListener = onTagClickListener;
  }

  public interface OnTagClickListener {
    void OnTagClick(String text, int position);
  }

  /**
   * 设置tab主色
   *
   * @param color
   */
  public void setColor(int color) {
    mColor = color;
  }

}
