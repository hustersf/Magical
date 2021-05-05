package com.sofar.aurora.feature.play.signal;

public enum PlayControlSignal {

  SEEK_POSITION,
  UPDATE_PROGRESS,
  SHOW_LRC,
  SHOW_DISC,
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
