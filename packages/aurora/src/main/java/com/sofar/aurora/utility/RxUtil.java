package com.sofar.aurora.utility;

import io.reactivex.disposables.Disposable;

public class RxUtil {

  public static  void dispose(Disposable disposable) {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }

}
