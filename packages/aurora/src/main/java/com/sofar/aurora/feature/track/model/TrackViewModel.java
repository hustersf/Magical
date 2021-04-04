package com.sofar.aurora.feature.track.model;

import androidx.lifecycle.ViewModel;

import com.sofar.aurora.feature.song.SongDataManager;
import com.sofar.aurora.retrofit.api.ApiProvider;
import com.sofar.aurora.retrofit.gson.ResponseFunction;
import com.sofar.aurora.utility.RxUtil;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class TrackViewModel extends ViewModel {

  public PublishSubject<TrackResponse> loadSuccess = PublishSubject.create();
  public PublishSubject<Throwable> loadFailed = PublishSubject.create();

  Disposable disposable;
  String trackId;

  public void query(String trackId) {
    this.trackId = trackId;
    RxUtil.dispose(disposable);
    disposable = ApiProvider.getApiService().trackList(trackId).map(new ResponseFunction<>())
      .subscribe(response -> {
        SongDataManager.get().put(trackId, response.songs);
        loadSuccess.onNext(response);
      }, throwable -> {
        loadFailed.onNext(throwable);
      });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    RxUtil.dispose(disposable);
    SongDataManager.get().remove(trackId);
  }

}
