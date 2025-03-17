package com.sofar.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayer(context: Context) {

  // 播放器事件监听
  private val playerListener = object : Player.Listener {
    private var isPlaying = false
    override fun onPlaybackStateChanged(state: Int) {
      when (state) {
        Player.STATE_BUFFERING -> {}

        Player.STATE_READY -> {
          if (player.isPlaying) {
            if (player.currentPosition > 0) {
              playerCallback?.onPlayerResume()
            } else {
              playerCallback?.onPlayerStart()
            }
          }
        }

        Player.STATE_ENDED -> {
          playerCallback?.onPlayerCompleted()
        }

        Player.STATE_IDLE -> {
          playerCallback?.onPlayerStop()
        }
      }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
      if (this.isPlaying && !isPlaying) {
        playerCallback?.onPlayerPause()
      }
      this.isPlaying = isPlaying
    }

    override fun onPlayerError(error: PlaybackException) {
      playerCallback?.onPlayerError(Exception(error.message))
    }
  }

  private var player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
    addListener(playerListener)
  }

  fun setDataSource(uri: String) {
    val mediaItem = MediaItem.fromUri(uri)
    player.setMediaItem(mediaItem)
  }

  fun start() {
    player.prepare()
    player.play()
  }

  fun resume() {
    player.play()
  }

  fun pause() {
    player.pause()
  }

  fun stop() {
    player.stop()
  }

  fun release() {
    stop()
    player.removeListener(playerListener)
    player.release()
    playerCallback = null
  }

  // 播放器状态回调接口
  interface PlayerCallback {
    fun onPlayerStart()
    fun onPlayerResume()
    fun onPlayerPause()
    fun onPlayerStop()
    fun onPlayerCompleted()
    fun onPlayerError(error: Exception)
  }

  private var playerCallback: PlayerCallback? = null

  // 设置回调监听
  fun setPlayerCallback(callback: PlayerCallback) {
    this.playerCallback = callback
  }


  // 扩展属性
  var volume: Float
    get() = player.volume
    set(value) {
      player.volume = value.coerceIn(0f, 1f)
    }

  val duration: Long
    get() = player.duration

  val currentPosition: Long
    get() = player.currentPosition

  fun seekTo(position: Long) {
    player.seekTo(position)
  }
}