package com.sofar.player.core;

import android.view.TextureView;
import androidx.annotation.NonNull;

/**
 * 视频播放器
 */
public class VideoPlayer extends BasePlayer {

  public VideoPlayer(@NonNull TextureView textureView) {
    super(textureView.getContext());
    player.setVideoTextureView(textureView);
    TAG = "VideoPlayer";
  }

  public VideoPlayer(@NonNull String uri, @NonNull TextureView textureView) {
    super(textureView.getContext(), uri);
    player.setVideoTextureView(textureView);
    TAG = "VideoPlayer";
  }

  @Override
  public void prepare() {
    super.prepare();
  }

  @Override
  public void stop() {
    super.stop();
  }
}
