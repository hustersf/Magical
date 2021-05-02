package com.sofar.aurora.feature.song.binder;

import androidx.annotation.NonNull;

import com.sofar.aurora.feature.play.PlayActivity;
import com.sofar.aurora.feature.play.PlayManager;
import com.sofar.aurora.feature.song.SongAdapter;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class SongClickViewBinder extends RecyclerViewBinder<Song> {

  @NonNull
  SongAdapter mAdapter;

  public SongClickViewBinder(@NonNull SongAdapter adapter) {
    mAdapter = adapter;
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    view.setOnClickListener(v -> {
      PlayManager.get().list(mAdapter.getList(), viewAdapterPosition);
      PlayActivity.launch(context);
    });
  }
}
