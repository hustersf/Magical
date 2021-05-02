package com.sofar.aurora.feature.song;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.song.binder.SongClickViewBinder;
import com.sofar.aurora.feature.song.binder.SongMoreViewBinder;
import com.sofar.aurora.feature.song.binder.SongOrderViewBinder;
import com.sofar.aurora.feature.song.binder.SongSummaryViewBinder;
import com.sofar.aurora.feature.song.binder.SongTitleViewBinder;
import com.sofar.aurora.model.Song;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class SongAdapter extends RecyclerAdapter<Song> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.song_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Song> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new SongOrderViewBinder());
    viewBinder.addViewBinder(new SongTitleViewBinder());
    viewBinder.addViewBinder(new SongSummaryViewBinder());
    viewBinder.addViewBinder(new SongMoreViewBinder());
    viewBinder.addViewBinder(new SongClickViewBinder(this));
    return viewBinder;
  }
}
