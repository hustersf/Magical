package com.sofar.aurora.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Song {

  @SerializedName(value = "id", alternate = "assetId")
  public String songId;

  @SerializedName("path")
  public String playUrl;

  @SerializedName("lyric")
  public String lyricUrl;

  @SerializedName("albumAssetCode")
  public String albumId;

  @SerializedName("duration")
  public int duration;

  @SerializedName("pic")
  public String url;

  @SerializedName("title")
  public String title;

  @SerializedName("albumTitle")
  public String albumTitle;

  @SerializedName("introduce")
  public String introduce;

  @SerializedName("genre")
  public String genre;

  @SerializedName("lang")
  public String lang;

  @SerializedName("artist")
  public List<Artist> artists;

  @SerializedName("trail_audio_info")
  public Part songPart;

  public static class Part {
    @SerializedName("start_time")
    public int startTime;

    @SerializedName("duration")
    public int duration;

    @SerializedName("path")
    public String playUrl;
  }

}
