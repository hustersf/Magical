package com.sofar.aurora.feature.play.binder;

import android.widget.ImageView;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.play.PlayContext;
import com.sofar.aurora.feature.play.PlayManager;
import com.sofar.aurora.feature.play.PlayMode;
import com.sofar.utility.ToastUtil;

public class PlayCycleViewBinder extends PlayBaseViewBinder {

  ImageView cycleIv;

  int playMode;

  @Override
  protected void onCreate() {
    super.onCreate();
    cycleIv = bindView(R.id.cycle);
  }

  @Override
  protected void onBind(PlayContext data) {
    super.onBind(data);
    playMode = PlayManager.get().playMode;
    updateUI();
    cycleIv.setOnClickListener(v -> {
      changePlayMode();
    });
  }

  private void changePlayMode() {
    String text = "";
    switch (playMode) {
      case PlayMode.LIST:
        playMode = PlayMode.RANDOM;
        text = "随机播放";
        break;
      case PlayMode.RANDOM:
        playMode = PlayMode.SINGLE;
        text = "单曲循环";
        break;
      case PlayMode.SINGLE:
        playMode = PlayMode.LIST;
        text = "列表循环";
        break;
    }
    PlayManager.get().setPlayMode(playMode);
    ToastUtil.startShort(context, text);
    updateUI();
  }

  private void updateUI() {
    if (playMode == PlayMode.RANDOM) {
      cycleIv.setImageResource(R.drawable.play_random_cycle_selector);
    } else if (playMode == PlayMode.SINGLE) {
      cycleIv.setImageResource(R.drawable.play_single_cycle_selector);
    } else {
      cycleIv.setImageResource(R.drawable.play_list_cycle_selector);
    }
  }
}
