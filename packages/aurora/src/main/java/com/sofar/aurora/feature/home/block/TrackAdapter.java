package com.sofar.aurora.feature.home.block;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.TrackClickViewBinder;
import com.sofar.aurora.feature.home.block.binder.TrackItemViewBinder;
import com.sofar.aurora.model.Track;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class TrackAdapter extends RecyclerAdapter<Track> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.block_item_track_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Track> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new TrackItemViewBinder());
    viewBinder.addViewBinder(new TrackClickViewBinder());
    return viewBinder;
  }
}
