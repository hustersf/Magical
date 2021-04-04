package com.sofar.aurora.retrofit.api;

import com.sofar.aurora.feature.home.model.HomeBlockResponse;
import com.sofar.aurora.feature.track.model.TrackResponse;
import com.sofar.aurora.retrofit.gson.Response;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

  @GET("v1/index")
  Observable<HomeBlockResponse> homeBlockList();

  @GET("v1/tracklist/info")
  Observable<Response<TrackResponse>> trackList(@Query("id") String trackId);

}
