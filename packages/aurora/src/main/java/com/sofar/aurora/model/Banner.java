package com.sofar.aurora.model;

import com.google.gson.annotations.SerializedName;

public class Banner {

  @SerializedName("title")
  public String title;

  @SerializedName("pic")
  public String imageUrl;

  @SerializedName("jumpType")
  public String jumpType;

}
