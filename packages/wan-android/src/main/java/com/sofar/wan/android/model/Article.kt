package com.sofar.wan.android.model

import com.google.gson.annotations.SerializedName

class Article {

  @SerializedName("id")
  var id: Long = 0

  @SerializedName("title")
  var title: String = ""

  @SerializedName("niceDate")
  var niceDate: String = ""

  @SerializedName("publishTime")
  var publishTime: Long = 0

  @SerializedName("author")
  var author: String = ""

  @SerializedName("shareUser")
  var shareUser: String = ""

  @SerializedName("link")
  var link: String = ""

  @SerializedName("chapterName")
  var chapterName: String = ""

  @SerializedName("superChapterName")
  var superChapterName: String = ""

  @SerializedName("collect")
  var collect: Boolean = false

  @SerializedName("type")
  var type: Int = 0

  @SerializedName("fresh")
  var fresh: Boolean = false

  @SerializedName("tags")
  var tags: List<Tag> = emptyList()

  /**
   * 获取文章作者
   */
  fun getArticleAuthor(): String = author.ifEmpty { shareUser }

}