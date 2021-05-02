package com.sofar.aurora.feature.play.signal;

public enum PlayControlSignal {

  SONG_SELECT;

  private Object mTag;

  public PlayControlSignal setTag(Object tag) {
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
