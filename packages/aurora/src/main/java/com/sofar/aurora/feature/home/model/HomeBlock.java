package com.sofar.aurora.feature.home.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HomeBlock<T> {

  @SerializedName("type")
  public String type;

  @SerializedName("module_name")
  public String name;

  @SerializedName("haveMore")
  public int haveMore;  //0 没有，1还有

  public List<T> results;

}
