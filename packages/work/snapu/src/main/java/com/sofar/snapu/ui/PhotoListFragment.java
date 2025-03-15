package com.sofar.snapu.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.sofar.base.page.PageList;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.recycler.RecyclerFragment;
import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.model.PhotoPageList;
import com.sofar.utility.DeviceUtil;
import com.sofar.widget.recycler.StaggeredGridMarginItemDecoration;

public class PhotoListFragment extends RecyclerFragment<ImageInfo> {

  long taskId;

  @Override
  protected RecyclerAdapter<ImageInfo> onCreateAdapter() {
    return new PhotoAdapter();
  }

  @Override
  protected PageList<?, ImageInfo> onCreatePageList() {
    return new PhotoPageList(getActivity(), taskId);
  }

  @Override
  protected RecyclerView.LayoutManager onCreateLayoutManager() {
    return new StaggeredGridLayoutManager(3, RecyclerView.VERTICAL);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    taskId = getArguments().getLong(PhotoListActivity.KEY_TASK_ID);
    super.onViewCreated(view, savedInstanceState);
    int itemSpace = DeviceUtil.dp2px(getContext(), 2);
    StaggeredGridMarginItemDecoration itemDecoration = new StaggeredGridMarginItemDecoration(3, itemSpace, 0, 0);
    getRecyclerView().addItemDecoration(itemDecoration);
  }
}
