package com.sofar.aurora.feature.play.binder;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.exception.SofarErrorConsumer;
import com.sofar.image.widget.SofarImageView;

import io.reactivex.functions.Consumer;

public class PlayDiscViewBinder extends PlayBaseViewBinder {

  ImageView discIv;
  ImageView needleIv;
  SofarImageView coverIv;

  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        if (playControlSignal.getTag() instanceof Song) {
          updateUI((Song) playControlSignal.getTag());
        }
        break;
    }
  };

  AnimatorSet mAnimatorSet;

  @Override
  protected void onCreate() {
    super.onCreate();
    discIv = bindView(R.id.disc);
    needleIv = bindView(R.id.needle);
    coverIv = bindView(R.id.cover);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    if (data.playSong == null) {
      return;
    }

    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    updateUI(data.playSong);
    startCoverAnim();
  }

  private void updateUI(Song song) {
    coverIv.bindUrl(song.url);
  }

  private void startCoverAnim() {
    mAnimatorSet = new AnimatorSet();
    ObjectAnimator animator = ObjectAnimator.ofFloat(coverIv, "rotation", 0, 360f);
    animator.setInterpolator(new LinearInterpolator());
    animator.setDuration(5000);
    animator.setRepeatMode(ValueAnimator.RESTART);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mAnimatorSet != null) {
      mAnimatorSet.cancel();
    }
  }
}
