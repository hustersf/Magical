package com.sofar.auto.play

interface PlayCallback {

  fun onPlayFinished(playable: Playable) {}

  fun onAllFinished() {}

  fun onPlayStart() {}

  fun onPlayStop() {}

}