package com.sofar.aurora.feature.home.block.binder;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Song;
import com.sofar.aurora.utility.NumberUtil;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;

public class SongItemViewBinder extends RecyclerViewBinder<Song> {

  SofarImageView coverIv;
  TextView titleTv;
  TextView summaryTv;
  ImageView playIv;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverIv = bindView(R.id.cover);
    titleTv = bindView(R.id.title);
    summaryTv = bindView(R.id.summary);
    playIv = bindView(R.id.play_icon);
  }

  @Override
  protected void onBind(Song data) {
    super.onBind(data);
    coverIv.bindUrl(data.url);
    titleTv.setText(data.title);
    summaryTv.setText(NumberUtil.formatPlayDuration(data.duration));
    playIv.setColorFilter(ContextCompat.getColor(context, com.sofar.base.R.color.theme_color));
  }
}
