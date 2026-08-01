package com.sofar.aurora.feature.home.block.binder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Album;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;

public class AlbumItemViewBinder extends RecyclerViewBinder<Album> {

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
  protected void onBind(Album data) {
    super.onBind(data);
    ImageExtKt.loadImage(coverIv, data.url);
    titleTv.setText(data.title);
    if (data.artists != null && !data.artists.isEmpty()) {
      summaryTv.setVisibility(View.VISIBLE);
      summaryTv.setText(data.artists.get(0).name);
    } else {
      summaryTv.setVisibility(View.GONE);
    }
  }
}
