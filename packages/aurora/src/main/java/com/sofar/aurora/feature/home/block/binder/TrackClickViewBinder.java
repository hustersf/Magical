package com.sofar.aurora.feature.home.block.binder;

import com.sofar.aurora.feature.track.TrackActivity;
import com.sofar.aurora.model.Track;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class TrackClickViewBinder extends RecyclerViewBinder<Track> {

  @Override
  protected void onBind(Track data) {
    super.onBind(data);
    view.setOnClickListener(v -> TrackActivity.launch(context, data));
  }
}
