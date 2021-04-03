package com.sofar.aurora.feature.home.block.binder;

import android.view.View;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Album;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.utility.CollectionUtil;

public class AlbumItemViewBinder extends RecyclerViewBinder<Album> {

  SofarImageView coverIv;
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
  protected void onBind(Album data) {
    super.onBind(data);
    coverIv.bindUrl(data.url);
    titleTv.setText(data.title);
    if (!CollectionUtil.isEmpty(data.artists)) {
      summaryTv.setVisibility(View.VISIBLE);
      summaryTv.setText(data.artists.get(0).name);
    } else {
      summaryTv.setVisibility(View.GONE);
    }
  }
}
