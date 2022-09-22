package com.sofar.wan.android.network.model

import com.google.gson.annotations.SerializedName
import com.sofar.wan.android.model.Article

class ArticleResponse {

  @SerializedName("curPage")
  var curPage: Int = 0

  @SerializedName("offset")
  var offset: Int = 0

  @SerializedName("total")
  var total: Int = 0

  @SerializedName("datas")
  var items: List<Article>? = null
}