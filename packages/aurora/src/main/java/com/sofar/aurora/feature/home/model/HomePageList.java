package com.sofar.aurora.feature.home.model;

import com.sofar.aurora.retrofit.api.ApiProvider;
import com.sofar.base.page.retrofit.SofarRetrofitPageList;

import io.reactivex.Observable;

public class HomePageList extends SofarRetrofitPageList<HomeBlockResponse, HomeBlock> {

  @Override
  protected Observable<HomeBlockResponse> onCreateRequest() {
    return ApiProvider.getApiService().homeBlockList();
  }

}
