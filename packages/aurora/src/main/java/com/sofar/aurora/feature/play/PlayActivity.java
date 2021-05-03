package com.sofar.aurora.feature.play;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.binder.PlayBackgroundViewBinder;
import com.sofar.aurora.feature.play.binder.PlayControlViewBinder;
import com.sofar.aurora.feature.play.binder.PlayCycleViewBinder;
import com.sofar.aurora.feature.play.binder.PlayDiscViewBinder;
import com.sofar.aurora.feature.play.binder.PlayListViewBinder;
import com.sofar.aurora.feature.play.binder.PlayLrcViewBinder;
import com.sofar.aurora.feature.play.binder.PlayTitleViewBinder;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.feature.play.signal.PlayStateSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.BaseActivity;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.widget.swipe.SwipeBack;
import com.sofar.widget.swipe.SwipeLayout;

import io.reactivex.subjects.PublishSubject;

public class PlayActivity extends BaseActivity {

  ViewBinder playViewBinder = new ViewBinder();

  PublishSubject<PlayControlSignal> mPlayControlSignal = PublishSubject.create();
  PublishSubject<PlayStateSignal> mPlayStateSignal = PublishSubject.create();

  PlayListener listener = new PlayListener() {
    @Override
    public void onSelect(@NonNull Song song) {
      mPlayControlSignal.onNext(PlayControlSignal.SONG_SELECT.setTag(song));
      PlayControlSignal.SONG_SELECT.reset();
    }

    @Override
    public void onPlay() {
      mPlayStateSignal.onNext(PlayStateSignal.PLAYING);
    }

    @Override
    public void onPause() {
      mPlayStateSignal.onNext(PlayStateSignal.PAUSE);
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.play_activity);
    SwipeBack.attach(this).setDirection(SwipeLayout.SwipeDirection.DOWN);
    PlayManager.get().register(listener);

    playViewBinder.addViewBinder(new PlayBackgroundViewBinder());
    playViewBinder.addViewBinder(new PlayLrcViewBinder());
    playViewBinder.addViewBinder(new PlayDiscViewBinder());
    playViewBinder.addViewBinder(new PlayControlViewBinder());
    playViewBinder.addViewBinder(new PlayCycleViewBinder());
    playViewBinder.addViewBinder(new PlayListViewBinder());
    playViewBinder.addViewBinder(new PlayTitleViewBinder());
    playViewBinder.create(getWindow().getDecorView().findViewById(android.R.id.content));
    PlayContext playContext = new PlayContext();
    playContext.playSong = PlayManager.get().playSong;
    playContext.mPlayControlSignal = mPlayControlSignal;
    playContext.mPlayStateSignal = mPlayStateSignal;
    playViewBinder.bind(playContext);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    playViewBinder.destroy();
    PlayManager.get().unregister(listener);
  }

  public static void launch(@NonNull Context context) {
    Intent intent = new Intent(context, PlayActivity.class);
    context.startActivity(intent);
  }

  @Override
  protected int finishExitPageAnim() {
    return R.anim.bottom_slide_out;
  }
}
