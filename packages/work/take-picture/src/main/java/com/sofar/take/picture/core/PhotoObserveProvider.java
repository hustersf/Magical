package com.sofar.take.picture.core;


import com.sofar.base.BaseActivity;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class PhotoObserveProvider {


  public static void start(@NonNull BaseActivity activity) {
    Observable.just("1")
      .flatMap(new OpenCameraObservable(activity))
      .doOnNext(new PhotoRotateConsumer(activity))
      .map(new PhotoInfoReadFunction())
      .doOnNext(new PhotoThumbSaveConsumer(activity))
      .doOnNext(new PhotoPreviewConsumer(activity))
      .subscribe();
  }

}
