package com.sofar.aurora.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Video {

  @SerializedName("assetCode")
  public String id;

  @SerializedName("type")
  public int type;

  @SerializedName("pic")
  public String coverUrl;

  @SerializedName("path")
  public String videoUrl;

  @SerializedName("width")
  public int width;

  @SerializedName("height")
  public int height;

  @SerializedName("introduce")
  public String introduce;

  @SerializedName("title")
  public String title;

  @SerializedName("duration")
  public int duration;

  @SerializedName("genreName")
  public String genre;

  @SerializedName("langCode")
  public String langCode;

  @SerializedName("artist")
  public List<Artist> artists;
}
