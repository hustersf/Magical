package com.sofar.aurora.feature.play.binder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.model.Song;
import com.sofar.base.blur.BlurUtil;
import com.sofar.base.exception.SofarErrorConsumer;
import com.sofar.image.FrescoUtil;
import com.sofar.utility.BitmapUtil;
import com.sofar.utility.DeviceUtil;

import io.reactivex.functions.Consumer;

public class PlayBackgroundViewBinder extends PlayBaseViewBinder {

  View playRoot;
  Bitmap blurBitmap;

  Consumer<PlayControlSignal> mPlayControlSignalConsumer = playControlSignal -> {
    switch (playControlSignal) {
      case SONG_SELECT:
        if (playControlSignal.getTag() instanceof Song) {
          update((Song) playControlSignal.getTag());
        }
        break;
    }
  };

  @Override
  protected void onCreate() {
    super.onCreate();
    playRoot = bindView(R.id.play_root);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    if (data.playSong == null) {
      return;
    }
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer, new SofarErrorConsumer()));
    update(data.playSong);
  }

  private void update(@NonNull Song song) {
    if (song.url == null) {
      return;
    }

    int size = DeviceUtil.dp2px(context, 25);
    FrescoUtil.fetchImage(song.url, bitmap -> {
      Bitmap smallBitmap = BitmapUtil.resizeBitmap(bitmap, size, size);
      blurBitmap = BlurUtil.getBlurBitmap(context, smallBitmap, 25);
      playRoot.setBackground(new BitmapDrawable(context.getResources(), blurBitmap));
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (blurBitmap != null) {
      blurBitmap.recycle();
    }
  }
}
