package com.sofar.aurora.model;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

public class Menu {

  @SerializedName("name")
  public String name;

  @SerializedName("pic")
  public String iconUrl;

  @SerializedName("module")
  public String module;

  @DrawableRes
  public int resId;

}
