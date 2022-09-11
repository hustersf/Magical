package com.sofar.widget.recycler.adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class Cell<T> {

  public RecyclerView.ViewHolder mViewHolder;

  protected abstract View createView();

  protected void onCreate(@NonNull View rootView) {}

  protected void onBind(T data) {}

  protected void onUnbind() {}

  protected void onDestroy() {}

  protected void onViewAttached() {}

  protected void onViewDetached() {}
}
