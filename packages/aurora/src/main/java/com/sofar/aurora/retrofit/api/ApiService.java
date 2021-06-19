package com.sofar.aurora.retrofit.api;

import com.sofar.aurora.feature.album.model.AlbumResponse;
import com.sofar.aurora.feature.home.model.HomeBlockResponse;
import com.sofar.aurora.feature.track.model.TrackResponse;
import com.sofar.aurora.model.Song;
import com.sofar.aurora.retrofit.gson.Response;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

  @GET("v1/index")
  Observable<HomeBlockResponse> homeBlockList();

  @GET("v1/tracklist/info")
  Observable<Response<TrackResponse>> trackList(@Query("id") String trackId);

  @GET("v1/album/info")
  Observable<Response<AlbumResponse>> albumList(@Query("albumAssetCode") String albumId);

  @GET("v1/song/tracklink")
  Observable<Response<Song>> songDetail(@Query("TSID") String songId);

  /**
   * 获取歌词内容
   */
  @GET()
  Observable<String> getLrc(@Url String url);

}
