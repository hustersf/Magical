package com.sofar.wan.android.model

import com.google.gson.annotations.SerializedName

class Article {

  @SerializedName("id")
  var id: Long = 0

  @SerializedName("title")
  var title: String = ""

  @SerializedName("publishTime")
  var publishTime: Long = 0

  @SerializedName("author")
  var author: String = ""

  @SerializedName("link")
  var link: String = ""

  @SerializedName("chapterName")
  var chapterName: String = ""

  @SerializedName("superChapterName")
  var superChapterName: String = ""

  @SerializedName("collect")
  var collect: Boolean = false

}