package com.sofar.auto.play

import android.util.Log
import java.util.LinkedList
import java.util.Queue

internal class FeedPlayerInternal(private val player: FeedPlayer) {

  companion object {
    private const val TAG = "FeedPlayerInternal"
  }

  private val playableList: Queue<Playable> = LinkedList()
  private var curPlayable: Playable? = null

  fun play(list: List<Playable>) {
    if (list.isNotEmpty()) {
      internalPlay(list)
      player.onStart()
    }
  }

  fun playNext(last: Playable?) {
    player.assetsPlayed(last)
    internalSchedule()
  }

  fun stop() {
    internalStop()
    player.onStop()
  }

  private fun internalPlay(list: List<Playable>) {
    playableList.clear()
    playableList.addAll(list)
    internalSchedule()
  }

  private fun internalSchedule() {
    val playable = playableList.poll()
    if (curPlayable === playable) {
      Log.d(TAG, "internalSchedule mCurrentPlay == playable")
      return
    }

    stopCurrent()
    if (playable != null) {
      curPlayable = playable
      playable.start()
    } else {
      player.allAssetsPlayed()
      Log.d(TAG, "player all finished")
    }
  }

  private fun stopCurrent() {
    curPlayable?.stop()
    curPlayable = null
  }

  private fun internalStop() {
    stopCurrent()
    playableList.clear()
  }

  fun release() {
  }

  fun getPlaying(): Playable? {
    return curPlayable
  }

  fun isPlaying(): Boolean {
    return curPlayable != null
  }
}