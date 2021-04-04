package com.sofar.aurora.feature.song.binder;

import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.CollectionUtil;

public class SongSummaryViewBinder extends RecyclerViewBinder<Song> {

  TextView summaryTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    summaryTv = bindView(R.id.summary);
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    StringBuffer sb = new StringBuffer();
    if (!CollectionUtil.isEmpty(data.artists)) {
      sb.append(data.artists.get(0).name);
      sb.append(" - ");
    }
    sb.append(data.albumTitle);
    summaryTv.setText(sb.toString());
  }
}
