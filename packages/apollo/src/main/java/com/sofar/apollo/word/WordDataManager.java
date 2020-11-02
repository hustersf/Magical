package com.sofar.apollo.word;

import androidx.annotation.NonNull;

import com.sofar.apollo.word.model.EnglishWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单词数据管理
 */
public class WordDataManager {

  List<EnglishWord> words = new ArrayList<>();

  Map<String, EnglishWord> wordMap = new HashMap<>();

  private static class Inner {
    private static WordDataManager INSTANCE = new WordDataManager();
  }

  private WordDataManager() {
  }

  public static WordDataManager get() {
    return Inner.INSTANCE;
  }

  @NonNull
  public List<EnglishWord> getWords() {
    return words;
  }

  public void setWords(@NonNull List<EnglishWord> list) {
    words.clear();
    words.addAll(list);
  }

}
