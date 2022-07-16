package com.sofar.depend.repo

class OpenSource {

  val gson = "com.google.code.gson:gson:2.8.6"
  val exoplayer = "com.google.android.exoplayer:exoplayer-core:2.12.1"

  val thinDownload = "com.mani:ThinDownloadManager:1.2.5"

  val rx = Rx()

  class Rx {
    val rxjava = "io.reactivex.rxjava2:rxjava:2.1.0"
    val rxandroid = "io.reactivex.rxjava2:rxandroid:2.1.0"

    val rxpermissions = "com.github.tbruyelle:rxpermissions:0.10.2"
  }

  val retrofit = Retrofit()

  class Retrofit {
    private val retrofitVersion = "2.6.1"
    val runtime = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    val gson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    val rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion"
    val scalars = "com.squareup.retrofit2:converter-scalars:$retrofitVersion"
  }

  val facebook = Facebook()

  class Facebook {
    private val frescoVersion = "2.3.0"
    val fresco = "com.facebook.fresco:fresco:$frescoVersion"
    val webp = "com.facebook.fresco:webpsupport:$frescoVersion"
    val infer = "com.facebook.infer.annotation:infer-annotation:0.18.0"
    val animatedWebp = "com.facebook.fresco:animated-webp:$frescoVersion"
    val animatedGif = "com.facebook.fresco:animated-gif:$frescoVersion"
    val animatedGifLite = "com.facebook.fresco:animated-gif-lite:$frescoVersion"
    val animatedBase = "com.facebook.fresco:animated-base:$frescoVersion"
    val uiCommon = "com.facebook.fresco:ui-common:$frescoVersion"
    val rebound = "com.facebook.rebound:rebound:0.3.8"
  }
}