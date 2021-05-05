package com.sofar.aurora.feature.play.lrc;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.R;
import com.sofar.utility.DeviceUtil;

public class LrcView extends View {

  private List<LrcEntry> mEntryList = new ArrayList<>();
  private TextPaint mPaint = new TextPaint();
  private float mTextSize;
  private float mDividerHeight;
  private long mAnimationDuration;
  private int mNormalColor;
  private int mCurrentColor;
  private float mLrcPadding;
  private ValueAnimator mAnimator;
  private float mAnimateOffset;
  private long mNextTime = 0L;
  private int mCurrentLine = 0;

  @NonNull
  private String mLabel;

  public LrcView(Context context) {
    this(context, null);
  }

  public LrcView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
    mTextSize =
      ta.getDimension(R.styleable.LrcView_lrcTextSize, DeviceUtil.dp2px(getContext(), 12));
    mDividerHeight =
      ta.getDimension(R.styleable.LrcView_lrcDividerHeight, DeviceUtil.dp2px(getContext(), 16));
    mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, 1000);
    mNormalColor = ta.getColor(R.styleable.LrcView_lrcNormalTextColor, 0xFFFFFFFF);
    mCurrentColor = ta.getColor(R.styleable.LrcView_lrcCurrentTextColor, 0xFFFF4081);
    mLabel = ta.getString(R.styleable.LrcView_lrcLabel);
    if (TextUtils.isEmpty(mLabel)) {
      mLabel = "暂无歌词";
    }
    mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, 0);
    ta.recycle();

    mPaint.setAntiAlias(true);
    mPaint.setTextSize(mTextSize);
    mPaint.setTextAlign(Paint.Align.LEFT);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.translate(0, mAnimateOffset);
    // 中心Y坐标
    float centerY = getHeight() / 2;
    mPaint.setColor(mCurrentColor);

    //没有歌词
    if (!hasLrc()) {
      drawEmptyLabel(canvas);
      return;
    }

    //绘制当前行
    float currY = centerY - getTextHeight(mCurrentLine) / 2;
    drawText(canvas, mEntryList.get(mCurrentLine).getStaticLayout(), currY);

    mPaint.setColor(mNormalColor);
    //绘制当前行上方的歌词
    float upY = currY;
    for (int i = mCurrentLine - 1; i >= 0; i--) {
      upY -= mDividerHeight + getTextHeight(i);

      if (mAnimator == null || !mAnimator.isRunning()) {
        // 动画已经结束，超出屏幕停止绘制
        if (upY < 0) {
          break;
        }
      }

      drawText(canvas, mEntryList.get(i).getStaticLayout(), upY);
      // 动画未结束，超出屏幕多绘制一行
      if (upY < 0) {
        break;
      }
    }

    //绘制当前行下方的歌词
    float downY = currY + getTextHeight(mCurrentLine) + mDividerHeight;
    for (int i = mCurrentLine + 1; i < mEntryList.size(); i++) {
      if (mAnimator == null || !mAnimator.isRunning()) {
        // 动画已经结束，超出屏幕停止绘制
        if (downY + getTextHeight(i) > getHeight()) {
          break;
        }
      }
      drawText(canvas, mEntryList.get(i).getStaticLayout(), downY);

      // 动画未结束，超出屏幕多绘制一行
      if (downY + getTextHeight(i) > getHeight()) {
        break;
      }
      downY += getTextHeight(i) + mDividerHeight;
    }
  }

  public void setEntryList(@NonNull List<LrcEntry> list) {
    mEntryList.clear();
    mEntryList.addAll(list);
    initEntryList();
    initNextTime();
    invalidate();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    initEntryList();
  }

  private void initEntryList() {
    if (getWidth() == 0) {
      return;
    }

    for (LrcEntry entry : mEntryList) {
      entry.buildStaticLayout(mPaint, getLrcWidth());
    }
  }

  private void initNextTime() {
    if (mEntryList.size() > 1) {
      mNextTime = mEntryList.get(1).time;
    } else {
      mNextTime = Long.MAX_VALUE;
    }
  }

  /**
   * @param time 根据时间刷新歌词
   */
  public void updateTime(long time) {
    // 避免重复绘制
    if (time < mNextTime) {
      return;
    }
    for (int i = mCurrentLine; i < mEntryList.size(); i++) {
      if (mEntryList.get(i).time > time) {
        mNextTime = mEntryList.get(i).time;
        mCurrentLine = (i < 1) ? 0 : (i - 1);
        newlineOnUI(i);
        break;
      } else if (i == mEntryList.size() - 1) {
        // 最后一行
        mCurrentLine = mEntryList.size() - 1;
        mNextTime = Long.MAX_VALUE;
        newlineOnUI(i);
        break;
      }
    }
  }

  /**
   * 滚动到指定歌词位置
   */
  public void drag(long time) {
    for (int i = 0; i < mEntryList.size(); i++) {
      if (mEntryList.get(i).time > time) {
        if (i == 0) {
          mCurrentLine = i;
          initNextTime();
        } else {
          mCurrentLine = i - 1;
          mNextTime = mEntryList.get(i).time;
        }
        newlineOnUI(i);
        break;
      }
    }
  }

  public void setCurrentColor(int color) {
    mCurrentColor = color;
  }

  public boolean hasLrc() {
    return !mEntryList.isEmpty();
  }

  private void newlineOnUI(final int index) {
    post(() -> newlineAnimation(index));
  }

  private void newlineAnimation(int index) {
    stopAnimation();

    mAnimator =
      ValueAnimator.ofFloat(getTextHeight(index) + mDividerHeight, 0.0f);
    if (mEntryList.get(index).getStaticLayout() != null) {
      mAnimator.setDuration(
        mAnimationDuration * mEntryList.get(index).getStaticLayout().getLineCount());
    }
    mAnimator.addUpdateListener(animation -> {
      mAnimateOffset = (float) animation.getAnimatedValue();
      invalidate();
    });
    mAnimator.start();
  }

  private void stopAnimation() {
    if (mAnimator != null && mAnimator.isRunning()) {
      mAnimator.end();
    }
  }

  private void drawEmptyLabel(Canvas canvas) {
    float textWidth = mPaint.measureText(mLabel);
    float x = getWidth() / 2 - textWidth / 2;
    canvas.drawText(mLabel, x, getHeight() / 2, mPaint);
  }

  private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
    canvas.save();
    canvas.translate(mLrcPadding, y);
    staticLayout.draw(canvas);
    canvas.restore();
  }

  private float getTextHeight(int index) {
    if (index >= 0 && index < mEntryList.size()) {
      return mEntryList.get(index).getTextHeight();
    }
    return 0;
  }

  private int getLrcWidth() {
    return (int) (getWidth() - mLrcPadding * 2);
  }

  /**
   * 清空歌词
   */
  public void clearLrc() {
    mEntryList.clear();
    mCurrentLine = 0;
    mNextTime = 0L;
    stopAnimation();
    invalidate();
  }
}
