package com.sofar.aurora.feature.play.lrc;

import java.io.Serializable;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class LrcEntry implements Serializable, Comparable<LrcEntry> {

  public long time;
  public String text;

  private StaticLayout mStaticLayout;
  private TextPaint mPaint;
  private int mWidth;

  public LrcEntry(long time, String text) {
    this.time = time;
    this.text = text;
  }

  void buildStaticLayout(TextPaint paint, int width) {
    this.mPaint = paint;
    this.mWidth = width;
    mStaticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER,
      1f, 0f, false);
  }

  StaticLayout getStaticLayout() {
    return mStaticLayout;
  }

  float getTextHeight() {
    if (mPaint == null || mStaticLayout == null) {
      return 0;
    }
    return mStaticLayout.getLineCount() * mPaint.getTextSize();
  }

  @Override
  public int compareTo(LrcEntry o) {
    if (this.time > o.time) {
      return 1;
    } else {
      return -1;
    }
  }

}
