package com.sofar.take.picture.ui;

import androidx.annotation.NonNull;

import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.take.picture.R;
import com.sofar.take.picture.model.ImageInfo;
import com.sofar.take.picture.viewbinder.PhotoItemViewBinder;

public class PhotoAdapter extends RecyclerAdapter<ImageInfo> {

  @Override
  protected int getItemLayoutId(int viewType) {
    return R.layout.photo_item;
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<ImageInfo> onCreateViewBinder(int viewType) {
    return new PhotoItemViewBinder();
  }

}
