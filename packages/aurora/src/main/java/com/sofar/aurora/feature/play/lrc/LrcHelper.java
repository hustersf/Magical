package com.sofar.aurora.feature.play.lrc;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;

import com.sofar.aurora.model.Song;
import com.sofar.aurora.retrofit.api.ApiProvider;
import com.sofar.utility.FileUtil;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LrcHelper {

  private static final String TAG = "LrcHelper";

  @NonNull
  Song mSong;
  @NonNull
  Context mContext;

  public LrcHelper(@NonNull Context context, @NonNull Song song) {
    mContext = context;
    mSong = song;
  }

  /**
   * 获取歌词
   */
  public Observable<String> getLrc() {
    return Observable.fromCallable(() -> {
      String diskLrc = getLrcFromFile();
      return diskLrc == null ? "" : diskLrc;
    }).flatMap(lrcStr -> {
      if (TextUtils.isEmpty(lrcStr)) {
        return getLrcFromServer();
      } else {
        return Observable.just(lrcStr);
      }
    }).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread());
  }

  private String getLrcFromFile() {
    String fileName = mSong.title + ".lrc";
    File file = new File(getLrcDir(mContext), fileName);
    if (file.exists() && file.length() > 0) {
      return FileUtil.getTextFromFile(file);
    } else {
      return null;
    }
  }

  private Observable<String> getLrcFromServer() {
    if (TextUtils.isEmpty(mSong.lyricUrl)) {
      return Observable.just("");
    }
    return ApiProvider.getApiService().getLrc(mSong.lyricUrl).doOnNext(s -> {
      saveLrcToFile(s);
    });
  }

  private void saveLrcToFile(String lrc) {
    try {
      File file = new File(getLrcFilePath());
      if (!file.exists()) {
        file.createNewFile();
      }
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(lrc.getBytes("utf-8"));
      fos.close();
    } catch (Exception e) {

    }
  }

  /**
   * 歌词本地存储文件路径
   */
  private String getLrcFilePath() {
    String fileName = mSong.title + ".lrc";
    return getLrcDir(mContext) + "/" + fileName;
  }

  /**
   * 歌词本地存储文件目录
   */
  public static String getLrcDir(Context context) {
    File lrcDir = new File(FileUtil.getCacheDir(context), "lrc");
    if (!lrcDir.exists()) {
      lrcDir.mkdirs();
    }
    return lrcDir.getAbsolutePath();
  }

}
