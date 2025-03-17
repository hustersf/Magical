package com.sofar.player

import android.content.Context
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoPlayer(context: Context) {

  private var player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
    addListener(object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
          Player.STATE_READY -> {}
          Player.STATE_ENDED -> {}
          Player.STATE_BUFFERING -> {}
          Player.STATE_IDLE -> {}
        }
      }
    })
  }

  fun setDataSource(uri: String) {
    val mediaItem = MediaItem.fromUri(uri)
    player.setMediaItem(mediaItem)
  }

  fun loopMode() {
    player.repeatMode = Player.REPEAT_MODE_ALL
  }

  fun setSurfaceView(surfaceView: SurfaceView) {
    player.setVideoSurfaceView(surfaceView)
  }

  fun setTextureView(textureView: TextureView) {
    player.setVideoTextureView(textureView)
  }

  fun setPlayerView(playerView: PlayerView) {
    playerView.player = player
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
    player.release()
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