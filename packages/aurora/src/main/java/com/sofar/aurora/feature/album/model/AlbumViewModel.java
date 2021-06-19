package com.sofar.aurora.feature.album.model;

import androidx.lifecycle.ViewModel;

import com.sofar.aurora.feature.song.SongDataManager;
import com.sofar.aurora.retrofit.api.ApiProvider;
import com.sofar.aurora.retrofit.gson.ResponseFunction;
import com.sofar.aurora.utility.RxUtil;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class AlbumViewModel extends ViewModel {

  public PublishSubject<AlbumResponse> loadSuccess = PublishSubject.create();
  public PublishSubject<Throwable> loadFailed = PublishSubject.create();

  Disposable disposable;
  String albumId;

  public void query(String albumId) {
    this.albumId = albumId;
    RxUtil.dispose(disposable);
    disposable = ApiProvider.getApiService().albumList(albumId).map(new ResponseFunction<>())
      .subscribe(response -> {
        SongDataManager.get().put(albumId, response.songs);
        loadSuccess.onNext(response);
      }, throwable -> {
        loadFailed.onNext(throwable);
      });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    RxUtil.dispose(disposable);
    SongDataManager.get().remove(albumId);
  }

}
