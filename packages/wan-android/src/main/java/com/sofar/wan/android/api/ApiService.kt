package com.sofar.wan.android.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

  /**
   * 首页banner
   */
  @GET("banner/json")
  suspend fun getBanner()

  /**
   * 首页文章
   */
  @GET("article/list/{pageNo}/json")
  suspend fun getArticlePageList(
    @Path("pageNo") pageNo: Int,
    @Query("page_size") pageSize: Int,
  )

  /**
   * 首页置顶文章
   */
  @GET("article/top/json")
  suspend fun getArticleTopList()

  /**
   * 广场文章
   */
  @GET("user_article/list/{pageNo}/json")
  suspend fun getSquarePageList(
    @Path("pageNo") pageNo: Int,
    @Query("page_size") pageSize: Int,
  )

  /**
   * 问答列表
   */
  @GET("wenda/list/{pageNo}/json")
  suspend fun getAnswerPageList(@Path("pageNo") pageNo: Int)
}