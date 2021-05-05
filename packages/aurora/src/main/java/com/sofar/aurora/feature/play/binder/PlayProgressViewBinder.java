package com.sofar.aurora.feature.play.binder;

import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.PlayManager;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.feature.play.signal.PlayStateSignal;
import com.sofar.aurora.utility.NumberUtil;
import com.sofar.base.exception.SofarErrorConsumer;

import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class PlayProgressViewBinder extends PlayBaseViewBinder {

  TextView playCurrentTv;
  TextView playDurationTv;
  SeekBar playSeekBar;

  boolean isSeeking;
  long seekPosition;
  PublishSubject<PlayControlSignal> mPlayControlSignal;

  Handler mHandler = new Handler(Looper.getMainLooper());
  Runnable mRunnable = new Runnable() {
    @Override
    public void run() {
      updateUI();
      mHandler.postDelayed(mRunnable, 1000);
    }
  };
  Consumer<PlayStateSignal> mPlayStateSignalConsumer = playStateSignal -> {
    switch (playStateSignal) {
      case PLAYING:
        updateUI();
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 1000);
        break;
      case PAUSE:
        mHandler.removeCallbacks(mRunnable);
        break;
    }
  };
  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        initProgress();
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    playCurrentTv = bindView(R.id.play_current);
    playDurationTv = bindView(R.id.play_duration);
    playSeekBar = bindView(R.id.play_seek_bar);
    playSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float ration = 1.0f * progress / playSeekBar.getMax();
        if (fromUser && PlayManager.get().getDuration() > 0) {
          seekPosition = (long) (PlayManager.get().getDuration() * ration);
          playCurrentTv.setText(NumberUtil.formatPlayDuration((int) (seekPosition / 1000)));
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        isSeeking = true;
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        isSeeking = false;
        PlayManager.get().seekTo(seekPosition);
        if (mPlayControlSignal != null) {
          mPlayControlSignal.onNext(PlayControlSignal.SEEK_POSITION.setTag(seekPosition));
        }
      }
    });
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    mDisposable
      .add(data.mPlayStateSignal.subscribe(mPlayStateSignalConsumer, new SofarErrorConsumer()));

    mPlayControlSignal = data.mPlayControlSignal;
    updateUI();
  }

  private void updateUI() {
    long currentPosition = PlayManager.get().getCurrentPosition();
    if (mPlayControlSignal != null) {
      mPlayControlSignal.onNext(PlayControlSignal.UPDATE_PROGRESS.setTag(currentPosition));
    }

    long duration = PlayManager.get().getDuration();
    if (duration <= 0 || isSeeking) {
      return;
    }

    playCurrentTv.setText(NumberUtil.formatPlayDuration((int) (currentPosition / 1000)));
    playDurationTv.setText(NumberUtil.formatPlayDuration((int) (duration / 1000)));
    playSeekBar.setProgress((int) (1.0f * playSeekBar.getMax() * currentPosition / duration));
  }

  private void initProgress() {
    playCurrentTv.setText("00:00");
    playDurationTv.setText("00:00");
    playSeekBar.setProgress(0);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mHandler.removeCallbacks(mRunnable);
  }
}
