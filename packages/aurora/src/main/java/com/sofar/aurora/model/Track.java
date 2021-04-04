package com.sofar.aurora.model;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Track implements Serializable {

  @SerializedName("id")
  public String id;

  @SerializedName("title")
  public String title;

  @SerializedName("desc")
  public String desc;

  @SerializedName("pic")
  public String url;

  @SerializedName("tagList")
  public List<String> tags;

  @SerializedName("cateList")
  public List<Integer> cateList;

  @SerializedName("trackCount")
  public int trackCount;

  @SerializedName("resourceType")
  public int resourceType;

}
