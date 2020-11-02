package com.sofar.base.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.sofar.utility.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetUtil {

  /**
   * 从asset目录下 读取图片
   */
  public static Bitmap getImageFromAssetsFile(@NonNull Context context, String fileName) {
    Bitmap image = null;
    AssetManager am = context.getResources().getAssets();
    InputStream is = null;
    try {
      is = am.open(fileName);
      image = BitmapFactory.decodeStream(is);
      is.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      FileUtil.closeQuietly(is);
    }
    return image;
  }

}
