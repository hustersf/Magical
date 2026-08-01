package com.sofar.base.rx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 基于 rxjava 实现的事件总线
 */
public class RxBus {

  private final Subject<Object> bus;
  private Map<Class<?>, Object> stickyEventMap;

  private static class Holder {
    static RxBus INSTANCE = new RxBus();
  }

  public static RxBus get() {
    return Holder.INSTANCE;
  }

  private RxBus() {
    bus = PublishSubject.create().toSerialized();
    stickyEventMap = new ConcurrentHashMap<>();
  }

  public void post(Object event) {
    bus.onNext(event);
  }

  public void postSticky(Object event) {
    stickyEventMap.put(event.getClass(), event);
    post(event);
  }

  public <T> Observable<T> toObservable(Class<T> eventType) {
    return toObservable(eventType, false);
  }

  public <T> Observable<T> toObservable(Class<T> eventType, boolean sticky) {
    Observable<T> observable = bus.ofType(eventType);
    if (sticky) {
      observable.startWith(observer -> {
        final Object object = stickyEventMap.get(eventType);
        if (object != null && eventType.isInstance(object)) {
          observer.onNext(eventType.cast(object));
          observer.onComplete();
        }
      });
    }
    return observable;
  }

  public void removeAllStickyEvents() {
    stickyEventMap.clear();
  }
}
