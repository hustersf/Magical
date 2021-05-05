package com.sofar.aurora.feature.play.binder;

import android.view.View;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.lrc.LrcHelper;
import com.sofar.aurora.feature.play.lrc.LrcUtil;
import com.sofar.aurora.feature.play.lrc.LrcView;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.exception.SofarErrorConsumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PlayLrcViewBinder extends PlayBaseViewBinder {

  View playLrcRoot;
  LrcView lrcView;

  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        lrcView.clearLrc();
        if (playControlSignal.getTag() instanceof Song) {
          updateUI((Song) playControlSignal.getTag());
        }
        break;
      case SHOW_LRC:
        playLrcRoot.setVisibility(View.VISIBLE);
        break;
      case UPDATE_PROGRESS:
        if (playControlSignal.getTag() instanceof Long) {
          lrcView.updateTime((Long) playControlSignal.getTag());
        }
        break;
      case SEEK_POSITION:
        if (playControlSignal.getTag() instanceof Long) {
          lrcView.drag((Long) playControlSignal.getTag());
        }
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    playLrcRoot = bindView(R.id.play_lrc_root);
    lrcView = bindView(R.id.lrc);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    if (data.playSong != null) {
      updateUI(data.playSong);
    }
    playLrcRoot.setOnClickListener(v -> {
      playLrcRoot.setVisibility(View.GONE);
      data.mPlayControlSignal.onNext(PlayControlSignal.SHOW_DISC);
    });
  }

  private void updateUI(@NonNull Song song) {
    LrcHelper lrcHelper = new LrcHelper(context, song);
    mDisposable.add(lrcHelper.getLrc()
      .map(LrcUtil::parseLrcList)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(list -> lrcView.setEntryList(list), new SofarErrorConsumer()));
  }
}
