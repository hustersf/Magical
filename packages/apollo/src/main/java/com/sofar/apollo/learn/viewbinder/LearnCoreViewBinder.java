package com.sofar.apollo.learn.viewbinder;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sofar.apollo.R;
import com.sofar.apollo.cache.AudioFileCache;
import com.sofar.apollo.word.WordDataManager;
import com.sofar.apollo.word.model.EnglishWord;
import com.sofar.base.viewbinder.ViewBinder;
import com.sofar.player.core.AudioPlayer;

import java.io.File;
import java.util.List;

public class LearnCoreViewBinder extends ViewBinder<LearnContext> {

  TextView wordTv;
  TextView soundTv;
  TextView chineseTv;

  TextView leftTv;
  TextView rightTv;

  ImageView voiceIv;
  AudioPlayer audioPlayer;

  @NonNull
  List<EnglishWord> list;

  int wordIndex;

  @Override
  protected void onCreate() {
    super.onCreate();
    wordTv = bindView(R.id.word);
    soundTv = bindView(R.id.sound);
    chineseTv = bindView(R.id.chinese);

    leftTv = bindView(R.id.left);
    rightTv = bindView(R.id.right);

    voiceIv = bindView(R.id.voice);

    list = WordDataManager.get().getWords();
  }

  @Override
  protected void onBind(LearnContext data) {
    super.onBind(data);

    update();
    leftTv.setOnClickListener(v -> {
      wordIndex--;
      update();
    });

    rightTv.setOnClickListener(v -> {
      wordIndex++;
      update();
    });

    wordTv.setOnClickListener(v -> {
      playAudio();
    });

    voiceIv.setOnClickListener(v -> {
      playAudio();
    });
  }

  private void update() {
    if (wordIndex >= 0 && wordIndex < list.size()) {
      EnglishWord item = list.get(wordIndex);
      wordTv.setText(item.word);
      soundTv.setText(item.sound);
      chineseTv.setText(item.chinese);
    }
  }

  private void playAudio() {
    if (wordIndex >= 0 && wordIndex < list.size()) {
      if (audioPlayer == null) {
        audioPlayer = new AudioPlayer(context);
      }
      EnglishWord item = list.get(wordIndex);
      String url = item.audioUrl;
      if (!TextUtils.isEmpty(url) && url.lastIndexOf("/") >= 0) {
        String fileName = url.substring(url.lastIndexOf("/"));
        File file = new File(AudioFileCache.getWordDir(context), fileName);
        audioPlayer.setUri(file.getAbsolutePath());
        audioPlayer.start();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (audioPlayer != null) {
      audioPlayer.stop();
      audioPlayer = null;
    }
  }
}
