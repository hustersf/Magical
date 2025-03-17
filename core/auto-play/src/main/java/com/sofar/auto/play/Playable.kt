package com.sofar.auto.play

interface Playable {

  fun start()

  fun stop()

  fun canPlay(): Boolean {
    return true
  }

  fun getViewShowRatio(): Float {
    return 0f
  }
}