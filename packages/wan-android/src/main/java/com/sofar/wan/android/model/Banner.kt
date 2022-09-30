package com.sofar.wan.android.model

import com.google.gson.annotations.SerializedName

class Banner {

  @SerializedName("id")
  var id: Long = 0

  @SerializedName("title")
  var title: String = ""

  @SerializedName("desc")
  var desc: String = ""

  @SerializedName("imagePath")
  var imageUrl: String = ""

  @SerializedName("url")
  var url: String = ""
}

class Banners(
  val banners: List<Banner>
)