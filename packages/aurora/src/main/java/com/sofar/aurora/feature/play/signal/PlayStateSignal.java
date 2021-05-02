package com.sofar.aurora.feature.play.signal;

public enum PlayStateSignal {

  PLAYING,
  PAUSE;

  private Object mTag;

  public PlayStateSignal setTag(Object tag) {
    mTag = tag;
    return this;
  }

  public Object getTag() {
    return mTag;
  }

  public void reset() {
    mTag = null;
  }
}
