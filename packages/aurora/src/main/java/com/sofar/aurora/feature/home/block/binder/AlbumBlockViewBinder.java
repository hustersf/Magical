package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.AlbumAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Album;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration;
import com.sofar.core.ui.util.DimensExtKt;

public class AlbumBlockViewBinder extends RecyclerViewBinder<HomeBlock<Album>> {

  @NonNull
  RecyclerView mRecyclerView;
  @NonNull
  AlbumAdapter mAdapter;

  @Override
  protected void onCreate() {
    super.onCreate();
    mRecyclerView = bindView(R.id.album_recycler);
    LinearLayoutManager layoutManager =
      new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
    int sideSpace = (int) context.getResources().getDimension(R.dimen.block_padding_left);
    int betweenSpace = DimensExtKt.dp2pxInt(context, 10);
    LinearMarginItemDecoration decoration =
      new LinearMarginItemDecoration(RecyclerView.HORIZONTAL, sideSpace, betweenSpace);
    mRecyclerView.addItemDecoration(decoration);
    mAdapter = new AlbumAdapter();
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onBind(HomeBlock<Album> data) {
    super.onBind(data);
    if (data.results != null && !data.results.isEmpty()) {
      mAdapter.setList(data.results);
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mRecyclerView.setAdapter(null);
  }
}
