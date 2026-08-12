package com.sofar.aurora.feature.play.binder;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.model.Song;
import com.sofar.core.common.extension.BitmapExtKt;
import com.sofar.core.ui.util.DimensExtKt;
import com.sofar.image.ImageExtKt;

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
    setDefaultBg();
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable
      .add(data.mPlayControlSignal.subscribe(mPlayControlSignalConsumer));
    if (data.playSong != null) {
      update(data.playSong);
    }
  }

  private void update(@NonNull Song song) {
    if (song.url == null) {
      return;
    }

    int size = DimensExtKt.dp2pxInt(context, 25);
    ImageExtKt.fetchImage(context, song.url, bitmap -> {
      Bitmap smallBitmap = BitmapExtKt.resize(bitmap, size, size);
      blurBitmap = BitmapExtKt.blur(smallBitmap, context, 25);
      playRoot.setBackground(new BitmapDrawable(context.getResources(), blurBitmap));
      return null;
    });
  }

  private void setDefaultBg() {
    Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    Bitmap defaultBitmap = BitmapExtKt.blur(bitmap, context, 25);
    playRoot.setBackground(new BitmapDrawable(context.getResources(), defaultBitmap));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (blurBitmap != null) {
      blurBitmap.recycle();
    }
  }
}
