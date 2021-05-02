package com.sofar.aurora.feature.play.binder;

import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.base.viewbinder.ViewBinder;

import io.reactivex.disposables.CompositeDisposable;

public class PlayBaseViewBinder extends ViewBinder<PlayContext> {

  CompositeDisposable mDisposable = new CompositeDisposable();

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    mDisposable.clear();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDisposable.clear();
  }
}
