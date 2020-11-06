package com.sofar.login.api;

import com.sofar.login.model.QQUserResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LoginApiService {

  /**
   * 连代理会抛错
   */
  @GET("https://graph.qq.com/user/get_user_info")
  Observable<QQUserResponse> loginWithQQ(@Query("oauth_consumer_key") String appId,
                                 @Query("access_token") String accessToken,
                                 @Query("openid") String openId);


}
