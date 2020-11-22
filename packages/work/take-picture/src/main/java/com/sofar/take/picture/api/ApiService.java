package com.sofar.take.picture.api;

import com.sofar.take.picture.model.LoginRequest;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {


  @POST("login")
  Observable<String> login(@Body LoginRequest request);

  @Multipart
  @POST("upload")
  Observable<String> uploadFile(@Part MultipartBody.Part file);

}
