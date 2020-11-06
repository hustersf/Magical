package com.sofar.login.model;

import com.google.gson.annotations.SerializedName;

public class User {

  /**
   * 用户id
   */
  @SerializedName("id")
  public String userId;

  /**
   * 用户名
   */
  @SerializedName("name")
  public String name;

  /**
   * 用户头像
   */
  @SerializedName("headUrl")
  public String headUrl;

  /**
   * 性别
   */
  @SerializedName("gender")
  public String gender;

}
