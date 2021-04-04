package com.sofar.aurora.feature.song.binder;

import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class SongMoreViewBinder extends RecyclerViewBinder<Song> {

  ImageView moreTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    moreTv = bindView(R.id.more);
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    moreTv.setOnClickListener(v -> {

    });
  }
}
