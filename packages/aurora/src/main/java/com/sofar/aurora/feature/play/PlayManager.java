package com.sofar.aurora.feature.play;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.aurora.SofarApp;
import com.sofar.aurora.model.Song;
import com.sofar.aurora.retrofit.api.ApiProvider;
import com.sofar.aurora.retrofit.gson.ResponseFunction;
import com.sofar.aurora.utility.RxUtil;
import com.sofar.player.core.AudioPlayer;
import com.sofar.player.core.OnPlayerListener;

import io.reactivex.disposables.Disposable;

/**
 * 管理音乐列表的播放
 */
public class PlayManager {

  private static final String TAG = "PlayManager";

  @NonNull
  List<Song> songs;
  @NonNull
  AudioPlayer player;

  int position = 0;
  Disposable disposable;

  @Nullable
  public Song playSong;

  List<PlayListener> listeners = new ArrayList<>();

  private static class Inner {
    static PlayManager INSTANCE = new PlayManager();
  }

  private PlayManager() {
    songs = new ArrayList<>();
    player = new AudioPlayer(SofarApp.getAppContext());
    player.setOnPlayerListener(new OnPlayerListener() {
      @Override
      public void onPlayerStart() {
        Log.d(TAG, "onPlayerStart");
      }

      @Override
      public void onPlayerResume() {
        Log.d(TAG, "onPlayerResume");
      }

      @Override
      public void onPlayerPause() {
        Log.d(TAG, "onPlayerPause");
      }

      @Override
      public void onPlayerStop() {
        Log.d(TAG, "onPlayerStop");
      }

      @Override
      public void onPlayerCompleted() {
        Log.d(TAG, "onPlayerCompleted");
        playNext();
      }

      @Override
      public void onPlayerError() {
        Log.d(TAG, "onPlayerError");
      }
    });
  }

  public static PlayManager get() {
    return Inner.INSTANCE;
  }

  /**
   * @param list 播放歌曲列表
   */
  public void list(@NonNull List<Song> list) {
    list(list, 0);
  }

  /**
   * @param list 播放歌曲列表
   */
  public void list(@NonNull List<Song> list, int position) {
    if (list.size() > 0 && position < list.size()) {
      songs.clear();
      songs.addAll(list);
      Song selectSong = list.get(position);
      this.position = position;
      setUri(selectSong);
    }
  }

  /**
   * @param playSong 播放选中的歌曲
   */
  public void select(@NonNull Song playSong) {
    if (!songs.contains(playSong)) {
      songs.add(playSong);
    }
    position = songs.indexOf(playSong);
    setUri(playSong);
  }

  /**
   * 播放
   */
  public void play() {
    player.start();
  }

  /**
   * 暂停
   */
  public void pause() {
    player.pause();
  }

  /**
   * 播放上一曲
   */
  public void playPrevious() {
    position--;
    if (position < 0) {
      position = songs.size() - 1;
    }
    Song song = songs.get(position);
    setUri(song);
  }

  /**
   * 播放下一曲
   */
  public void playNext() {
    position++;
    if (position >= songs.size() - 1) {
      position = 0;
    }
    Song song = songs.get(position);
    setUri(song);
  }

  /**
   * 清空列表
   */
  public void clear() {
    player.stop();
    songs.clear();
    RxUtil.dispose(disposable);
  }

  private void setUri(@NonNull Song item) {
    if (TextUtils.isEmpty(item.playUrl)) {
      RxUtil.dispose(disposable);
      disposable = ApiProvider.getApiService().songDetail(item.songId).map(new ResponseFunction<>())
        .subscribe(song -> {
          if (TextUtils.isEmpty(song.playUrl)) {
            item.playUrl = song.songPart.playUrl;
          } else {
            item.playUrl = song.playUrl;
          }
          setUriAndPlay(item);
        }, throwable -> {});
    } else {
      setUriAndPlay(item);
    }
  }

  private void setUriAndPlay(@NonNull Song item) {
    Log.d(TAG, "setUriAndPlay="+item.title);
    player.setUri(item.playUrl);
    play();
    onSelect(item);
  }


  private void onSelect(@NonNull Song song) {
    playSong = song;
    for (PlayListener listener : listeners) {
      listener.onSelect(song);
    }
  }

  public void register(PlayListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void unregister(PlayListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

}
