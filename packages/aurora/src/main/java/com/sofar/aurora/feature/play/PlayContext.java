package com.sofar.aurora.feature.play;

import com.sofar.aurora.feature.play.signal.PlayControlSignal;
import com.sofar.aurora.feature.play.signal.PlayStateSignal;
import com.sofar.aurora.model.Song;

import io.reactivex.subjects.PublishSubject;

public class PlayContext {

  public Song playSong;

  public PublishSubject<PlayControlSignal> mPlayControlSignal = PublishSubject.create();

  public PublishSubject<PlayStateSignal> mPlayStateSignal = PublishSubject.create();
}
