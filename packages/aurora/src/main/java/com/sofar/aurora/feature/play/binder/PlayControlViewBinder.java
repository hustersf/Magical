package com.sofar.aurora.feature.play.binder;

import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.PlayManager;
import com.sofar.aurora.feature.play.signal.PlayStateSignal;
import com.sofar.base.exception.SofarErrorConsumer;

import io.reactivex.functions.Consumer;

public class PlayControlViewBinder extends PlayBaseViewBinder {

  ImageView playIv;
  ImageView preIv;
  ImageView nextIv;

  boolean isPlay = true;

  Consumer<PlayStateSignal> mPlayStateSignalConsumer = playStateSignal -> {
    switch (playStateSignal) {
      case PLAYING:
        isPlay = true;
        updateUI();
        break;
      case PAUSE:
        isPlay = false;
        updateUI();
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    playIv = bindView(R.id.play);
    preIv = bindView(R.id.pre);
    nextIv = bindView(R.id.next);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    updateUI();
    playIv.setOnClickListener(v -> {
      if (isPlay) {
        PlayManager.get().pause();
        data.mPlayStateSignal.onNext(PlayStateSignal.PAUSE);
      } else {
        PlayManager.get().play();
        data.mPlayStateSignal.onNext(PlayStateSignal.PLAYING);
      }
    });
    preIv.setOnClickListener(v -> {
      PlayManager.get().playPrevious();
    });
    nextIv.setOnClickListener(v -> {
      PlayManager.get().playNext();
    });

    mDisposable
      .add(data.mPlayStateSignal.subscribe(mPlayStateSignalConsumer, new SofarErrorConsumer()));
  }

  private void updateUI() {
    playIv
      .setImageResource(isPlay ? R.drawable.play_pause_selector : R.drawable.play_play_selector);
  }
}
