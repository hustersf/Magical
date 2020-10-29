package com.sofar.apollo.book.model;

import com.google.gson.annotations.SerializedName;

public class Book {

  @SerializedName("title")
  public String title;

  @SerializedName("description")
  public String description;

  @SerializedName("language")
  public String language;

  @SerializedName("tag")
  public String tag;

  @SerializedName("imgUrl")
  public String imgUrl;

}
