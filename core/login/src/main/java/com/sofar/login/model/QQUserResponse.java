package com.sofar.login.model;

import com.google.gson.annotations.SerializedName;

public class QQUserResponse {

  /**
   * 用户id
   */
  @SerializedName("openid")
  public String userId;

  /**
   * 用户名
   */
  @SerializedName("nickname")
  public String name;

  /**
   * 用户头像
   */
  @SerializedName("figureurl_qq")
  public String headUrl;

  /**
   * 性别
   */
  @SerializedName("gender")
  public String gender;

  /**
   * 性别
   */
  @SerializedName("gender_type")
  public int gender_type;

  /**
   * 省份
   */
  @SerializedName("province")
  public String province;

  /**
   * 城市
   */
  @SerializedName("city")
  public String city;

}
