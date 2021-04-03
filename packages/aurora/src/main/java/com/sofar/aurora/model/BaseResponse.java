package com.sofar.aurora.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

  @SerializedName("state")
  public boolean state;

  @SerializedName("errno")
  public int code;

  @SerializedName("errmsg")
  public String message;

  @SerializedName("elapsed_time")
  public String elapsedTime;

}
