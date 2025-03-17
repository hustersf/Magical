package com.sofar.auto.play

class FeedPlayer {

  private var playableList: List<Playable>? = null
  private var internal: FeedPlayerInternal? = null
  private var playCallback: PlayCallback? = null

  init {
    internal = FeedPlayerInternal(this)
  }

  fun feed(list: List<Playable>): FeedPlayer {
    playableList = list
    return this
  }

  fun callback(callback: PlayCallback?): FeedPlayer {
    playCallback = callback
    return this
  }

  fun start() {
    playableList?.let {
      internal?.play(it)
    }
  }

  fun stop() {
    internal?.stop()
  }

  fun playFinished(playable: Playable?) {
    internal?.playNext(playable)
  }

  fun release() {
    internal?.release()
  }

  fun getPlaying(): Playable? {
    return internal?.getPlaying()
  }

  fun isPlaying(): Boolean {
    return internal?.isPlaying() ?: false
  }

  fun assetsPlayed(playable: Playable?) {
    playCallback?.onPlayFinished(playable!!)
  }

  fun allAssetsPlayed() {
    playCallback?.onAllFinished()
  }

  fun onStart() {
    playCallback?.onPlayStart()
  }

  fun onStop() {
    playCallback?.onPlayStop()
  }
}