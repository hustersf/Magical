package com.sofar.aurora.feature.song.binder;

import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class SongOrderViewBinder extends RecyclerViewBinder<Song> {

  TextView orderTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    orderTv = bindView(R.id.order);
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    orderTv.setText(String.valueOf(viewAdapterPosition + 1));
  }
}
