package com.sofar.aurora.feature.home.block.binder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.block.SongAdapter;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Song;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.CollectionUtil;
import com.sofar.utility.DeviceUtil;
import com.sofar.widget.recycler.GridMarginItemDecoration;

public class SongBlockViewBinder extends RecyclerViewBinder<HomeBlock<Song>> {

  @NonNull
  RecyclerView mRecyclerView;
  @NonNull
  SongAdapter mAdapter;

  @Override
  protected void onCreate() {
    super.onCreate();
    mRecyclerView = bindView(R.id.song_recycler);
    int spanCount = 3;
    GridLayoutManager layoutManager =
      new GridLayoutManager(context, spanCount, RecyclerView.HORIZONTAL, false);
    int sideSpace = (int) context.getResources().getDimension(R.dimen.block_padding_left);
    int betweenSpace = DeviceUtil.dp2px(context, 10);
    GridMarginItemDecoration decoration =
      new GridMarginItemDecoration(RecyclerView.HORIZONTAL, spanCount, betweenSpace, sideSpace,
        sideSpace);

    mRecyclerView.addItemDecoration(decoration);
    mAdapter = new SongAdapter();
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(mAdapter);
    PagerSnapHelper snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(mRecyclerView);
  }

  @Override
  protected void onBind(HomeBlock<Song> data) {
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
