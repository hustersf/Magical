package com.sofar.apollo.cache;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sofar.core.common.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 音频文件缓存
 */
public class AudioFileCache {

  public static final String WORD_DIR = "speech";

  /**
   * @param context
   * @return 单词音频文件存储目录
   */
  public static String getWordDir(@NonNull Context context) {
    File dir = new File(FileUtil.getFileDir(context), WORD_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir.getAbsolutePath();
  }

  /**
   * @param context
   * @param name    音频文件名字
   * @param toDir   本地存储目录
   * @return
   */
  public static String copyWordAssetsToDir(@NonNull Context context, String name, String toDir) {
    String toFile = toDir + File.separator + name;

    File fileDir = new File(toDir);
    if (!fileDir.exists()) {
      fileDir.mkdirs();
    }

    // 使用 Try-with-resources 自动管理资源
    try (InputStream is = context.getAssets().open("test/" + WORD_DIR + File.separator + name);
         OutputStream os = new FileOutputStream(toFile)) {

      byte[] bytes = new byte[8192];
      int byteCount;
      while ((byteCount = is.read(bytes)) != -1) {
        os.write(bytes, 0, byteCount);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return toFile;
  }

}
