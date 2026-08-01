package com.sofar.apollo.learn.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.core.common.extension.BitmapExtKt;
import com.sofar.core.common.util.FileUtil;
import com.sofar.core.ui.util.WindowExtKt;

/**
 * 高斯模糊最大25，但是不够模糊
 * 先对图片进行比例压缩，得到一张模糊的图，然后在进行高斯模糊
 */
public class LearnViewBinder extends ViewBinder<LearnContext> {

  ViewGroup root;
  Bitmap blurImg;

  @Override
  protected void onCreate() {
    super.onCreate();
    root = view.findViewById(R.id.learn_root);
  }

  @Override
  protected void onBind(LearnContext data) {
    super.onBind(data);
    Bitmap srcBitmap = FileUtil.getImageFromAssetsFile(context, "img/home_img.jpg");
    int width = WindowExtKt.getScreenWidth(context) / 10;
    int height = WindowExtKt.getScreenHeight(context) / 10;
    Bitmap smallBitmap = BitmapExtKt.resize(srcBitmap, width, height);
    srcBitmap.recycle();
    blurImg = BitmapExtKt.blur(smallBitmap, context, 25);
    root.setBackground(new BitmapDrawable(context.getResources(), blurImg));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (blurImg != null) {
      blurImg.recycle();
    }
  }
}
