package com.sofar.apollo.learn.viewbinder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.sofar.apollo.R;
import com.sofar.base.blur.BlurUtil;
import com.sofar.base.util.AssetUtil;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.utility.BitmapUtil;
import com.sofar.utility.DeviceUtil;

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
    Bitmap srcBitmap = AssetUtil.getImageFromAssetsFile(context, "img/home_img.jpg");
    int width = DeviceUtil.getMetricsWidth(context) / 10;
    int height = DeviceUtil.getMetricsHeight(context) / 10;
    Bitmap smallBitmap = BitmapUtil.resizeBitmap(srcBitmap, width, height);
    srcBitmap.recycle();
    blurImg = BlurUtil.getBlurBitmap(context, smallBitmap, 25);
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
