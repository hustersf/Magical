package com.sofar.wan.android.network.model

import com.google.gson.annotations.SerializedName

class Response<T> {

  @SerializedName("errorCode")
  var errorCode: Int = -1

  @SerializedName("errorMsg")
  var errorMsg: String? = ""

  @SerializedName("data")
  var data: T? = null

}