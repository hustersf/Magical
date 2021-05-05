package com.sofar.aurora.feature.play.binder;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.feature.play.signal.PlayStateSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.exception.SofarErrorConsumer;
import com.sofar.image.widget.SofarImageView;

import io.reactivex.functions.Consumer;

public class PlayDiscViewBinder extends PlayBaseViewBinder {

  View playDiscRoot;
  ImageView discIv;
  ImageView needleIv;
  SofarImageView coverIv;

  ObjectAnimator coverAnimator;

  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        if (playControlSignal.getTag() instanceof Song) {
          updateUI((Song) playControlSignal.getTag());
        }
        break;
      case SHOW_DISC:
        playDiscRoot.setVisibility(View.VISIBLE);
        break;
    }
  };
  Consumer<PlayStateSignal> mPlayStateSignalConsumer = playStateSignal -> {
    switch (playStateSignal) {
      case PLAYING:
        if (coverAnimator != null) {
          coverAnimator.resume();
        }
        needlePlayAnim();
        break;
      case PAUSE:
        if (coverAnimator != null) {
          coverAnimator.pause();
        }
        needlePauseAnim();
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    playDiscRoot = bindView(R.id.play_disc_root);
    discIv = bindView(R.id.disc);
    needleIv = bindView(R.id.needle);
    coverIv = bindView(R.id.cover);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    mDisposable
      .add(data.mPlayStateSignal.subscribe(mPlayStateSignalConsumer, new SofarErrorConsumer()));
    startCoverAnim();

    if (data.playSong != null) {
      updateUI(data.playSong);
    }

    playDiscRoot.setOnClickListener(v -> {
      playDiscRoot.setVisibility(View.GONE);
      data.mPlayControlSignal.onNext(PlayControlSignal.SHOW_LRC);
    });
  }

  private void updateUI(Song song) {
    coverIv.bindUrl(song.url);
  }

  private void startCoverAnim() {
    coverAnimator = ObjectAnimator.ofFloat(coverIv, "rotation", 0, 360f);
    coverAnimator.setInterpolator(new LinearInterpolator());
    coverAnimator.setDuration(10000);
    coverAnimator.setRepeatMode(ValueAnimator.RESTART);
    coverAnimator.setRepeatCount(ValueAnimator.INFINITE);
    coverAnimator.start();
  }

  private void needlePauseAnim() {
    ObjectAnimator animator = ObjectAnimator.ofFloat(needleIv, "rotation", 0, -20f);
    needleIv.setPivotX(0);
    needleIv.setPivotY(0);
    animator.setDuration(500);
    animator.start();
  }

  private void needlePlayAnim() {
    ObjectAnimator animator = ObjectAnimator.ofFloat(needleIv, "rotation", -20f, 0);
    needleIv.setPivotX(0);
    needleIv.setPivotY(0);
    animator.setDuration(500);
    animator.start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (coverAnimator != null) {
      coverAnimator.cancel();
    }
  }
}
