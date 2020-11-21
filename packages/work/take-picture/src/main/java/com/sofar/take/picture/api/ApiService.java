package com.sofar.take.picture.api;


import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {


  @GET("https://graph.qq.com/user/get_user_info")
  Observable<String> login(@Query("phone") String phone, @Query("password") String password);

}
