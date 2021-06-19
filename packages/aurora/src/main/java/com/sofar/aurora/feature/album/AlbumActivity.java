package com.sofar.aurora.feature.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.sofar.aurora.feature.album.model.AlbumViewModel;
import com.sofar.aurora.feature.song.SongActivity;

/**
 * 专辑页
 */
public class AlbumActivity extends SongActivity {

  @NonNull
  AlbumViewModel mViewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
    fetch();
  }


  private void fetch() {
    mViewModel.query(id);
    mViewModel.loadSuccess.subscribe(response -> {
      onSuccess();
    });
    mViewModel.loadFailed.subscribe(throwable -> {
      onFailed(throwable);
    });
  }

  public static void launch(@NonNull Context context, @NonNull String albumId) {
    Intent intent = new Intent(context, AlbumActivity.class);
    intent.putExtra(KEY_ID, albumId);
    context.startActivity(intent);
  }

}
