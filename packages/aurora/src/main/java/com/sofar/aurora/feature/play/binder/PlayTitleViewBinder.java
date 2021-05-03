package com.sofar.aurora.feature.play.binder;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.exception.SofarErrorConsumer;

import io.reactivex.functions.Consumer;

public class PlayTitleViewBinder extends PlayBaseViewBinder {

  ImageView backIv;
  TextView titleTv;

  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        if (playControlSignal.getTag() instanceof Song) {
          updateUI((Song) playControlSignal.getTag());
        }
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    backIv = bindView(R.id.back);
    titleTv = bindView(R.id.title);
    backIv.setOnClickListener(v -> {
      if (getActivity() != null) {
        getActivity().finish();
      }
    });
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    if (data.playSong != null) {
      updateUI(data.playSong);
    }
  }

  private void updateUI(@NonNull Song song) {
    titleTv.setText(song.title);
  }
}
