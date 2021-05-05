package com.sofar.aurora.feature.home.block.binder;

import android.view.View;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Video;
import com.sofar.aurora.utility.NumberUtil;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.utility.CollectionUtil;

public class VideoItemViewBinder extends RecyclerViewBinder<Video> {

  SofarImageView coverIv;
  TextView titleTv;
  TextView summaryTv;
  TextView durationTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverIv = bindView(R.id.cover);
    titleTv = bindView(R.id.title);
    summaryTv = bindView(R.id.summary);
    durationTv = bindView(R.id.duration);
  }

  @Override
  protected void onBind(Video data) {
    super.onBind(data);
    coverIv.bindUrl(data.coverUrl);
    titleTv.setText(data.title);
    if (!CollectionUtil.isEmpty(data.artists)) {
      summaryTv.setVisibility(View.VISIBLE);
      summaryTv.setText(data.artists.get(0).name);
    } else {
      summaryTv.setVisibility(View.GONE);
    }
    durationTv.setText(NumberUtil.formatPlayDuration(data.duration / 1000));
  }
}
