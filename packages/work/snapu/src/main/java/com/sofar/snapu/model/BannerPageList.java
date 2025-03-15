package com.sofar.snapu.model;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sofar.base.page.retrofit.SofarRetrofitPageList;
import com.sofar.snapu.api.ApiRetrofitConfig;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BannerPageList extends SofarRetrofitPageList<BannerResponse, Banner> {

  @NonNull
  Activity activity;

  public BannerPageList(@NonNull Activity activity) {
    this.activity = activity;
  }

  @Override
  protected Observable<BannerResponse> onCreateRequest() {
    return Observable.fromCallable(() -> {
      BannerResponse response = new BannerResponse();
      List<Banner> list = new ArrayList<>();

      Banner banner = new Banner();
      banner.imgUrl = ApiRetrofitConfig.baseUrl + "photo";
      list.add(banner);
      response.items = list;
      Log.d("PhotoPageList", "thread=" + Thread.currentThread().getName());
      return response;
    }).subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread());
  }

}
