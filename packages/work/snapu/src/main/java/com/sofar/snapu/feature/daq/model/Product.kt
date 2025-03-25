package com.sofar.snapu.feature.daq.model

import com.google.gson.annotations.SerializedName

class Product {

  @SerializedName("id")
  var taskId: Long = 0

  @SerializedName("ean")
  var ean: String = ""

  @SerializedName("name")
  var name: String = ""

  @SerializedName("imageFile")
  var imageFile: String = ""

  @SerializedName("videoFile")
  var videoFile: String = ""

  @SerializedName("videoFrameFile")
  var videoFrameFile: String = ""

  @SerializedName("uploaded")
  var uploaded: Boolean = false

  @SerializedName("version")
  var version: Int = 0

}
