package com.sofar.aurora.feature.home.block;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.TrackClickViewBinder;
import com.sofar.aurora.feature.home.block.binder.TrackItemViewBinder;
import com.sofar.aurora.model.Track;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class TrackAdapter extends RecyclerAdapter<Track> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_track_item, parent, false);
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
