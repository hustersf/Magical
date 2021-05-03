package com.sofar.aurora.feature.play;

import androidx.annotation.NonNull;

import com.sofar.aurora.model.Song;

public interface PlayListener {

  default void onSelect(@NonNull Song song) {}

  default void onPlay() {}

  default void onPause() {}

}
