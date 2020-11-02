package com.sofar.apollo.word.model;

import com.google.gson.annotations.SerializedName;

public class EnglishWord {

  @SerializedName("word")
  public String word;

  @SerializedName("chinese")
  public String chinese;

  @SerializedName("sound")
  public String sound;

  @SerializedName("audioUrl")
  public String audioUrl;
}
