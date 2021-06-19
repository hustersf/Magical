package com.sofar.aurora.feature.track;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.sofar.aurora.feature.song.SongActivity;
import com.sofar.aurora.feature.track.model.TrackViewModel;

/**
 * 歌单页
 */
public class TrackActivity extends SongActivity {

  @NonNull
  TrackViewModel trackViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    trackViewModel = new ViewModelProvider(this).get(TrackViewModel.class);
    fetch();
  }


  private void fetch() {
    trackViewModel.query(id);
    trackViewModel.loadSuccess.subscribe(response -> {
      onSuccess();
    });
    trackViewModel.loadFailed.subscribe(throwable -> {
      onFailed(throwable);
    });
  }

  public static void launch(@NonNull Context context, @NonNull String trackId) {
    Intent intent = new Intent(context, TrackActivity.class);
    intent.putExtra(KEY_ID, trackId);
    context.startActivity(intent);
  }

}
