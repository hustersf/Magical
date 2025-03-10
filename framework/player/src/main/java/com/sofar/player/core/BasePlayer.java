package com.sofar.player.core;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * 封装播放器的使用
 * 基于 ExoPlayer
 */
public abstract class BasePlayer {

  protected String TAG = "BasePlayer";

  @NonNull
  SimpleExoPlayer player;
  @NonNull
  Context context;

  boolean prepared;
  boolean completed;

  @Nullable
  OnPlayerListener playerListener;

  Player.Listener eventListener = new Player.Listener() {
    @Override
    public void onPlaybackStateChanged(int state) {
      Log.d(TAG, "onPlaybackStateChanged:state=" + state);
      switch (state) {
        case Player.STATE_ENDED:
          completed = true;
          if (playerListener != null) {
            playerListener.onPlayerCompleted();
          }
          break;
        case Player.STATE_READY:
          prepared = true;
          break;
        case Player.STATE_BUFFERING:
          prepared = false;
          break;
      }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
      Log.d(TAG, "onPlayerError=" + error.getMessage());
      if (playerListener != null) {
        playerListener.onPlayerError();
      }
    }
  };

  public BasePlayer(@NonNull Context context) {
    this.context = context;
    player = new SimpleExoPlayer.Builder(context).build();
    player.addListener(eventListener);
  }

  public BasePlayer(@NonNull Context context, @NonNull String uri) {
    this.context = context;
    player = new SimpleExoPlayer.Builder(context).build();
    player.addListener(eventListener);
    MediaItem mediaItem = MediaItem.fromUri(uri);
    player.setMediaItem(mediaItem);
  }

  /**
   * 重置播放源
   */
  public void setUri(@NonNull String uri) {
    MediaItem mediaItem = MediaItem.fromUri(uri);
    player.setMediaItem(mediaItem);
  }

  public void prepare() {
    player.prepare();
  }

  /**
   * 是否准备好
   */
  public boolean isPrepared() {
    return prepared;
  }

  public void start() {
    Log.d(TAG, "start");
    if (!isPrepared()) {
      prepare();
    }

    if (completed) {
      player.seekTo(0);
      completed = false;
    }
    player.play();
  }

  public void resume() {
    Log.d(TAG, "resume");
    player.play();
    if (playerListener != null) {
      playerListener.onPlayerResume();
    }
  }

  public void pause() {
    Log.d(TAG, "pause");
    player.pause();
    if (playerListener != null) {
      playerListener.onPlayerPause();
    }
  }

  public void stop() {
    Log.d(TAG, "stop");
    player.stop();
    player.release();
    player.removeListener(eventListener);
    if (playerListener != null) {
      playerListener.onPlayerStop();
    }
  }

  public void setOnPlayerListener(OnPlayerListener listener) {
    this.playerListener = listener;
  }

  public long getDuration() {
    return player.getDuration();
  }

  public long getCurrentPosition() {
    return player.getCurrentPosition();
  }

  public void seekTo(long positionMs) {
    player.seekTo(positionMs);
  }

}
