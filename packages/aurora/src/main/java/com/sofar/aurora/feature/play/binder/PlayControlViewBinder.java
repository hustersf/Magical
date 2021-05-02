package com.sofar.aurora.feature.play.binder;

import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.PlayManager;

public class PlayControlViewBinder extends PlayBaseViewBinder {

  ImageView playIv;
  ImageView preIv;
  ImageView nextIv;

  boolean isPlay = true;

  @Override
  protected void onCreate() {
    super.onCreate();
    playIv = bindView(R.id.play);
    preIv = bindView(R.id.pre);
    nextIv = bindView(R.id.next);

    playIv.setOnClickListener(v -> {
      if (isPlay) {
        PlayManager.get().pause();
      } else {
        PlayManager.get().play();
      }
      isPlay = !isPlay;
      updateUI();
    });
    preIv.setOnClickListener(v -> {
      PlayManager.get().playPrevious();
    });
    nextIv.setOnClickListener(v -> {
      PlayManager.get().playNext();
    });
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    updateUI();
  }

  private void updateUI() {
    playIv
      .setImageResource(isPlay ? R.drawable.play_pause_selector : R.drawable.play_play_selector);
  }
}
