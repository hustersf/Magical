package com.sofar.aurora.feature.track.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.sofar.aurora.model.Song;
import com.sofar.base.page.response.ListResponse;

public class TrackResponse implements ListResponse<Song> {

  @SerializedName("tagList")
  public List<String> tagList;

  @SerializedName("trackList")
  public List<Song> songs;

  @Override
  public boolean hasMore() {
    return false;
  }

  @Override
  public List<Song> getItems() {
    return songs;
  }
}
