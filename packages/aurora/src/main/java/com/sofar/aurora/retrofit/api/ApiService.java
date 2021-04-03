package com.sofar.aurora.retrofit.api;

import com.sofar.aurora.feature.home.model.HomeBlockResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface ApiService {

  @GET("v1/index")
  Observable<HomeBlockResponse> homeBlockList();

}
