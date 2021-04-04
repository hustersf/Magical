package com.sofar.aurora.feature.song.binder;

import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class SongTitleViewBinder extends RecyclerViewBinder<Song> {

  TextView titleTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    titleTv = bindView(R.id.title);
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    titleTv.setText(data.title);
  }
}
