package com.sofar.aurora.feature.home.block;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.ArtistItemViewBinder;
import com.sofar.aurora.model.Artist;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class ArtistAdapter extends RecyclerAdapter<Artist> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_artist_item, parent, false);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Artist> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new ArtistItemViewBinder());
    return viewBinder;
  }
}
