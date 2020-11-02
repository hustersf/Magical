package com.sofar.apollo.mock;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sofar.apollo.SofarApp;
import com.sofar.apollo.cache.AudioFileCache;
import com.sofar.apollo.word.model.EnglishWord;
import com.sofar.utility.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MockManager {

  private static class Inner {
    private static MockManager INSTANCE = new MockManager();
  }

  private MockManager() {
    setUpAudioFile();
  }

  public static MockManager get() {
    return Inner.INSTANCE;
  }

  /**
   * 将给定 test_data.json 转化为 标准的json数据
   */
  public List<EnglishWord> convertData(@NonNull Context context) {
    String testJson = FileUtil.getTextFromAssets(context, "test/test_data.json");
    Gson gson = new Gson();
    Map<String, List<String>> map = gson.fromJson(testJson, new TypeToken<LinkedHashMap<String, List<String>>>() {
    }.getType());

    Log.d("MockManager", "map size=" + map.size());
    List<EnglishWord> list = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      String key = entry.getKey();
      List<String> value = entry.getValue();
      if (value.size() < 3) {
        Log.d("MockManager", "单词：" + key + " 数据不合法");
        continue;
      }

      EnglishWord item = new EnglishWord();
      item.word = key;
      item.chinese = value.get(0);
      item.sound = value.get(1);
      item.audioUrl = value.get(2);
      list.add(item);
    }

    setUpAudioFile();
    return list;
  }

  /**
   * 将assets目录下的音频文件拷贝到手机目录下
   */
  private void setUpAudioFile() {
    try {
      Context context = SofarApp.getAppContext();
      String[] audioFiles = context.getAssets().list("test/speech");
      String fileDir = AudioFileCache.getWordDir(context);
      for (String fileName : audioFiles) {
        File file = new File(fileDir, fileName);
        if (!file.exists())
          AudioFileCache.copyWordAssetsToDir(context, fileName, fileDir);
      }
      Log.d("MockManager", "speech 音频文件已复制到目录:" + fileDir);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
