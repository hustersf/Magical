package com.sofar.aurora.model;

import com.google.gson.annotations.SerializedName;

public class Banner {

  @SerializedName("title")
  public String title;

  @SerializedName("pic")
  public String imageUrl;

  @SerializedName("jumpLinkOutput")
  public String jumpLinkOutput;

  @JumpType
  @SerializedName("jumpType")
  public String jumpType;

  public @interface JumpType {
    String TYPE_ALBUM = "2";
    String TYPE_TRACK = "3";
  }

}
