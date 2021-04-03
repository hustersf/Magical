package com.sofar.aurora.feature.home.model;

public class BlockContainer<T> {

  T data;

  public void set(T data) {
    this.data = data;
  }

  public T get() {
    return data;
  }

}
