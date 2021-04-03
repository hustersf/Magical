package com.sofar.aurora.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Album {

  @SerializedName("albumAssetCode")
  public String albumId;

  @SerializedName("type")
  public int type;

  @SerializedName("pic")
  public String url;

  @SerializedName("title")
  public String title;

  @SerializedName("introduce")
  public String introduce;

  @SerializedName("genre")
  public String genre;

  @SerializedName("lang")
  public String lang;

  @SerializedName("artist")
  public List<Artist> artists;
}
