package com.sofar.wan.android.model

import com.google.gson.annotations.SerializedName

class Tag {

  @SerializedName("name")
  var name: String = ""

  @SerializedName("url")
  var url: String = ""

  var selected = false
}