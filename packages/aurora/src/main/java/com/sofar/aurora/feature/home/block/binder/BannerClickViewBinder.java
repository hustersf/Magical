package com.sofar.aurora.feature.home.block.binder;

import android.text.TextUtils;

import com.sofar.aurora.feature.album.AlbumActivity;
import com.sofar.aurora.feature.track.TrackActivity;
import com.sofar.aurora.model.Banner;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BannerClickViewBinder extends RecyclerViewBinder<Banner> {

  @Override
  protected void onBind(Banner data) {
    super.onBind(data);
    view.setOnClickListener(v -> {
      if (TextUtils.equals(data.jumpType, Banner.JumpType.TYPE_TRACK)) {
        TrackActivity.launch(context, data.jumpLinkOutput);
      } else if (TextUtils.equals(data.jumpType, Banner.JumpType.TYPE_ALBUM)) {
        AlbumActivity.launch(context, data.jumpLinkOutput);
      }
    });
  }
}
