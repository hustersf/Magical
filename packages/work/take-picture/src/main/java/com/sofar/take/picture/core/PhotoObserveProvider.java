package com.sofar.take.picture.core;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class PhotoObserveProvider {

  int imageOrder = 0;

  public void startTask(@NonNull PhotoHelper helper, long taskId) {
    imageOrder++;
    Observable.just(taskId)
      .flatMap(new OpenCameraObservable(helper, imageOrder))
      .doOnNext(new PhotoRotateConsumer(helper))
      .map(new PhotoInfoReadFunction())
      .doOnNext(new PhotoAddTaskIdConsumer(helper))
      .doOnNext(new PhotoThumbSaveConsumer(helper))
      .doOnNext(new PhotoPreviewConsumer(helper))
      .subscribe();
  }

}
