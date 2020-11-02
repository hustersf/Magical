package com.sofar.base.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class BlurUtil {

  /**
   * @param bitmap 原始位图
   * @param view   目标View
   * @return 返回目标View区域的位图
   */
  public static Bitmap getTargetBitmap(Bitmap bitmap, View view) {
    Bitmap dstArea = Bitmap.createBitmap(view.getMeasuredWidth(), (view.getMeasuredHeight()),
      Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dstArea);
    canvas.translate(-view.getLeft(), -view.getTop());
    canvas.drawBitmap(bitmap, 0, 0, null);
    return dstArea;
  }

  /**
   * @param srcView 原始View
   * @param view    目标View
   * @return 返回目标View区域的位图
   */
  public static Bitmap getTargetBitmap(View srcView, View view) {
    srcView.buildDrawingCache();
    return getTargetBitmap(srcView.getDrawingCache(), view);
  }

  public static Bitmap getBlurBitmap(@NonNull Context context, @NonNull Bitmap bitmap, @IntRange(from = 1, to = 25) int radius) {
    return RenderBlur.gaussianBlur(context, bitmap, radius);
  }
}
