package com.sofar.snapu.core;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class PhotoObserveProvider {

  int imageOrder = 0;


  public void startTask(@NonNull PhotoHelper helper, String filePath) {
    imageOrder++;
    Observable.just(filePath)
      .doOnNext(new PhotoRotateConsumer(helper))
      .map(new PhotoInfoReadFunction())
      .doOnNext(new PhotoAddTaskIdConsumer(helper))
      .doOnNext(new PhotoThumbSaveConsumer(helper))
      .doOnNext(new PhotoPreviewConsumer(helper))
      .subscribe();
  }

}
