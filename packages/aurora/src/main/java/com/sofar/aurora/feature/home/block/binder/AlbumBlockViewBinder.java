package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.AlbumAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Album;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.CollectionUtil;
import com.sofar.utility.DeviceUtil;
import com.sofar.widget.recycler.LinearMarginItemDecoration;

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
    int betweenSpace = DeviceUtil.dp2px(context, 10);
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
    if (!CollectionUtil.isEmpty(data.results)) {
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
