package com.sofar.aurora.feature.home.block.binder;

import com.sofar.aurora.feature.album.AlbumActivity;
import com.sofar.aurora.model.Album;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class AlbumClickViewBinder extends RecyclerViewBinder<Album> {

  @Override
  protected void onBind(Album data) {
    super.onBind(data);
    view.setOnClickListener(v -> AlbumActivity.launch(context, data.albumId));
  }
}
