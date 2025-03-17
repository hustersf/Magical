package com.sofar.auto.play

import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerViewPlayer : PlayCallback {

  protected lateinit var recyclerView: RecyclerView
  protected var enable: Boolean = true

  protected var player: FeedPlayer = FeedPlayer()

  private var scrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      super.onScrollStateChanged(recyclerView, newState)
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        recapture()
      }
    }
  }


  fun attach(recyclerView: RecyclerView) {
    detach()
    this.recyclerView = recyclerView
    recyclerView.addOnScrollListener(scrollListener)
  }

  fun detach() {
    if (!this::recyclerView.isInitialized) {
      return
    }
    recyclerView.removeOnScrollListener(scrollListener)
  }

  abstract fun findPlayableList(): List<Playable>

  fun enable() {
    enable(true)
  }

  fun enable(recapture: Boolean) {
    enable = true
    if (recapture) {
      recapture()
    }
  }

  fun disable() {
    enable = false
    player.stop()
  }

  fun recapture() {
    if (enable) {
      val list: List<Playable> = findPlayableList()
      maybePlay(list)
    }
  }

  protected fun maybePlay(list: List<Playable>) {
    if (list.isEmpty()) {
      player.feed(emptyList())
      player.stop()
    } else {
      player.feed(list).start()
    }
  }
}