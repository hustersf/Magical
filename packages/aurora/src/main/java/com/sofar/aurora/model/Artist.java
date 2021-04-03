package com.sofar.aurora.model;

import com.google.gson.annotations.SerializedName;

public class Artist {

  @SerializedName("artistCode")
  public String artistId;

  @SerializedName("name")
  public String name;

  @SerializedName("pic")
  public String url;

  @SerializedName("gender")
  public String gender;

  @SerializedName("birthday")
  public String birthday;

  @SerializedName("artistType")
  public String artistType;

  @SerializedName("artistTypeName")
  public String artistTypeName;

  @SerializedName("albumTotal")
  public int albumTotal;

  @SerializedName("trackTotal")
  public int trackTotal;

  @SerializedName("favoriteCount")
  public int favoriteCount;

}
