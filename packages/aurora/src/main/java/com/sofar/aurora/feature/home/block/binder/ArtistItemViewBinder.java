package com.sofar.aurora.feature.home.block.binder;

import android.widget.ImageView;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Artist;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;

public class ArtistItemViewBinder extends RecyclerViewBinder<Artist> {

  ImageView coverIv;
  TextView nameTv;
  TextView summaryTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    coverIv = bindView(R.id.cover);
    nameTv = bindView(R.id.name);
    summaryTv = bindView(R.id.summary);
  }

  @Override
  protected void onBind(Artist data) {
    super.onBind(data);
    ImageExtKt.loadImage(coverIv, data.url);
    nameTv.setText(data.name);
    summaryTv.setText(data.birthday);
  }
}
