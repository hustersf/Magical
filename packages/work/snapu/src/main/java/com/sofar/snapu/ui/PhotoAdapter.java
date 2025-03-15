package com.sofar.snapu.ui;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.snapu.R;
import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.viewbinder.PhotoItemViewBinder;
import com.sofar.utility.ViewUtil;

public class PhotoAdapter extends RecyclerAdapter<ImageInfo> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.photo_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<ImageInfo> onCreateViewBinder(int viewType) {
    return new PhotoItemViewBinder();
  }

}
