package com.sofar.aurora.feature.home.block;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.binder.ArtistItemViewBinder;
import com.sofar.aurora.model.Artist;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class ArtistAdapter extends RecyclerAdapter<Artist> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.block_item_artist_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Artist> onCreateViewBinder(int viewType) {
    RecyclerViewBinder viewBinder = new RecyclerViewBinder();
    viewBinder.addViewBinder(new ArtistItemViewBinder());
    return viewBinder;
  }
}
