package com.sofar.aurora.feature.song;


import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.model.Song;

public class SongDataManager {

  @NonNull
  HashMap<String, List<Song>> map;

  private static class Inner {
    static SongDataManager INSTANCE = new SongDataManager();
  }

  private SongDataManager() {
    map = new HashMap<>();
  }

  public static SongDataManager get() {
    return Inner.INSTANCE;
  }

  public void put(String id, @NonNull List<Song> songs) {
    map.put(id, songs);
  }

  public void remove(String id) {
    map.remove(id);
  }

  @Nullable
  public List<Song> get(String id) {
    return map.get(id);
  }

  @Nullable
  public List<Song> getAndRemove(String id) {
    List<Song> songs = map.remove(id);
    return songs;
  }

}
