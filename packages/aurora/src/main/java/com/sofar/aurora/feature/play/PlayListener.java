package com.sofar.aurora.feature.play;

import androidx.annotation.NonNull;

import com.sofar.aurora.model.Song;

public interface PlayListener {

  void onSelect(@NonNull Song song);

}
