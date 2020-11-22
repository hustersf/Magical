package com.sofar.take.picture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.base.page.PageList;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.recycler.RecyclerFragment;
import com.sofar.take.picture.R;
import com.sofar.take.picture.model.Banner;
import com.sofar.take.picture.model.BannerPageList;
import com.sofar.take.picture.model.ImageInfo;

public class MainFragment extends RecyclerFragment<Banner> {

  TextView photoNumberTv;
  PagerSnapHelper snapHelper;

  TextView taskTv;
  TextView finishTv;

  RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        View view = snapHelper.findSnapView(layoutManager);
        int position = layoutManager.getPosition(view);
        showNumber(position);
      }
    }
  };

  @Override
  protected RecyclerAdapter<Banner> onCreateAdapter() {
    return new BannerAdapter();
  }

  @Override
  protected PageList<?, Banner> onCreatePageList() {
    return new BannerPageList(getActivity());
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.main_fragment;
  }

  @Override
  protected RecyclerView.LayoutManager onCreateLayoutManager() {
    return new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView();

    snapHelper = new PagerSnapHelper();
    snapHelper.attachToRecyclerView(getRecyclerView());
    getRecyclerView().addOnScrollListener(scrollListener);
  }

  private void initView() {
    photoNumberTv = getView().findViewById(R.id.photo_order);
    taskTv = getView().findViewById(R.id.task);
    finishTv = getView().findViewById(R.id.finish);

    finishTv.setOnClickListener(v -> getActivity().finish());
    taskTv.setOnClickListener(v -> createTask());
  }

  private void createTask() {
    Intent intent = new Intent(getContext(), TaskActivity.class);
    startActivity(intent);
  }

  private void showNumber(int position) {
    int max = getPageList().getItems().size();
    int cur = position + 1;
    photoNumberTv.setText(cur + "/" + max);
  }

  @Override
  public void onFinishLoading(boolean firstPage, boolean cache) {
    super.onFinishLoading(firstPage, cache);
    showNumber(0);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    getRecyclerView().removeOnScrollListener(scrollListener);
  }
}
