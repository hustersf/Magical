package com.sofar.aurora.feature.home.block;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.AlbumClickViewBinder;
import com.sofar.aurora.feature.home.block.binder.AlbumItemViewBinder;
import com.sofar.aurora.model.Album;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class AlbumAdapter extends RecyclerAdapter<Album> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_album_item, parent, false);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Album> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new AlbumItemViewBinder());
    viewBinder.addViewBinder(new AlbumClickViewBinder());
    return viewBinder;
  }
}
