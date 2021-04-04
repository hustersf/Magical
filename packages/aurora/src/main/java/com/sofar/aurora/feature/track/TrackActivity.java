package com.sofar.aurora.feature.track;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.song.SongFragment;
import com.sofar.aurora.feature.track.model.TrackViewModel;
import com.sofar.aurora.model.Track;
import com.sofar.aurora.retrofit.ExceptionHandler;
import com.sofar.base.BaseActivity;
import com.sofar.utility.statusbar.StatusBarUtil;

/**
 * 歌单页
 */
public class TrackActivity extends BaseActivity {

  public static final String KEY_TRACK = "track";

  @NonNull
  TrackViewModel trackViewModel;

  @NonNull
  Track track;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.track_activty);
    StatusBarUtil.setLightMode(this);
    if (getIntent() != null && getIntent().getSerializableExtra(KEY_TRACK) instanceof Track) {
      track = (Track) getIntent().getSerializableExtra(KEY_TRACK);
    } else {
      finish();
      return;
    }

    trackViewModel = new ViewModelProvider(this).get(TrackViewModel.class);
    fetch();
  }

  private void fetch() {
    trackViewModel.query(track.id);
    trackViewModel.loadSuccess.subscribe(response -> {
      replaceSongFragment();
    });
    trackViewModel.loadFailed.subscribe(throwable -> {
      ExceptionHandler.handleException(throwable);
    });
  }

  private void replaceSongFragment() {
    SongFragment fragment = SongFragment.create(track.id);
    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, fragment, "song")
      .commitAllowingStateLoss();
  }


  public static void launch(@NonNull Context context, @NonNull Track track) {
    Intent intent = new Intent(context, TrackActivity.class);
    intent.putExtra(KEY_TRACK, track);
    context.startActivity(intent);
  }

}
