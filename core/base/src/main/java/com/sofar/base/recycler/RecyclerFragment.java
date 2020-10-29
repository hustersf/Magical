package com.sofar.base.recycler;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sofar.base.BaseFragment;
import com.sofar.base.R;
import com.sofar.base.page.PageList;
import com.sofar.base.page.PageListObserver;

public abstract class RecyclerFragment<MODEL> extends BaseFragment implements PageListObserver {

  private final static String TAG = "RecyclerFragment";

  @Nullable
  private SwipeRefreshLayout mRefreshLayout;
  private View mRootView;
  private RecyclerView mRecyclerView;
  private RecyclerView.OnScrollListener mAutoLoadEventDetector;

  private RecyclerAdapter<MODEL> mOriginAdapter;

  private PageList<?, MODEL> mPageList;

  protected int getLayoutResId() {
    return R.layout.base_recycler_fragment;
  }

  protected RecyclerView.LayoutManager onCreateLayoutManager() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    return layoutManager;
  }

  protected abstract RecyclerAdapter<MODEL> onCreateAdapter();

  protected abstract PageList<?, MODEL> onCreatePageList();

  protected boolean autoLoad() {
    return true;
  }

  protected RecyclerView.OnScrollListener onCreateAutoLoadEventDetector() {
    return new AutoLoadEventDetector(this, getPageList());
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    mRootView = inflater.inflate(getLayoutResId(), container, false);
    mRefreshLayout = mRootView.findViewById(R.id.refresh_layout);
    mRecyclerView = mRootView.findViewById(R.id.recycler_view);
    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initRecyclerView();
    initRefreshLayout();

    mPageList = onCreatePageList();
    mPageList.registerObserver(this);
    mAutoLoadEventDetector = onCreateAutoLoadEventDetector();
    mRecyclerView.addOnScrollListener(mAutoLoadEventDetector);
    if (autoLoad()) {
      refresh();
    }
  }

  private void initRecyclerView() {
    mRecyclerView.setLayoutManager(onCreateLayoutManager());
    mOriginAdapter = onCreateAdapter();
    mRecyclerView.setAdapter(mOriginAdapter);
  }

  private void initRefreshLayout() {
    if (mRefreshLayout != null) {
      mRefreshLayout.setOnRefreshListener(() -> {
        refresh();
      });
    }
  }


  public void setRefreshEnable(boolean enable) {
    if (mRefreshLayout != null) {
      mRefreshLayout.setEnabled(enable);
    }
  }

  @NonNull
  public RecyclerView getRecyclerView() {
    return mRecyclerView;
  }

  @NonNull
  public RecyclerAdapter<MODEL> getOriginAdapter() {
    return mOriginAdapter;
  }

  @NonNull
  public PageList<?, MODEL> getPageList() {
    return mPageList;
  }

  private void refresh() {
    mPageList.refresh();
  }

  @Override
  public void onStartLoading(boolean firstPage, boolean cache) {
    Log.d(TAG, "onStartLoading");
  }

  @Override
  public void onFinishLoading(boolean firstPage, boolean cache) {
    Log.d(TAG, "onFinishLoading");
    if (mRefreshLayout != null) {
      mRefreshLayout.setRefreshing(false);
    }

    mOriginAdapter.setList(mPageList.getItems());
    mOriginAdapter.notifyDataSetChanged();
  }

  @Override
  public void onError(boolean firstPage, Throwable throwable) {
    Log.d(TAG, "onError=" + throwable.toString());
    if (mRefreshLayout != null) {
      mRefreshLayout.setRefreshing(false);
    }
  }
}

