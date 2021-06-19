package com.sofar.aurora.feature.album.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.sofar.aurora.model.Song;
import com.sofar.base.page.response.ListResponse;

public class AlbumResponse implements ListResponse<Song> {

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
