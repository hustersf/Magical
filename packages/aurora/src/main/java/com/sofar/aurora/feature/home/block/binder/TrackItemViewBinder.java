package com.sofar.aurora.feature.home.block.binder;

import android.widget.ImageView;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Track;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;

public class TrackItemViewBinder extends RecyclerViewBinder<Track> {

  ImageView coverIv;
  TextView titleTv;
  TextView summaryTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverIv = bindView(R.id.cover);
    titleTv = bindView(R.id.title);
    summaryTv = bindView(R.id.summary);
  }

  @Override
  protected void onBind(Track data) {
    super.onBind(data);
    ImageExtKt.loadImage(coverIv, data.url);
    titleTv.setText(data.title);
    summaryTv.setText(data.trackCount + "首单曲");
  }
}
