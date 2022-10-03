package com.sofar.wan.android.network.api

import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Banner
import com.sofar.wan.android.model.WxArticle
import com.sofar.wan.android.network.model.ArticleResponse
import com.sofar.wan.android.network.model.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

  /**
   * 首页banner
   */
  @GET("banner/json")
  suspend fun getBanner(): Response<List<Banner>>

  /**
   * 首页文章
   */
  @GET("article/list/{pageNo}/json")
  suspend fun getArticlePageList(
    @Path("pageNo") pageNo: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>

  /**
   * 首页置顶文章
   */
  @GET("article/top/json")
  suspend fun getArticleTopList(): Response<List<Article>>

  /**
   * 广场文章
   */
  @GET("user_article/list/{pageNo}/json")
  suspend fun getSquarePageList(
    @Path("pageNo") pageNo: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>

  /**
   * 问答列表
   */
  @GET("wenda/list/{pageNo}/json")
  suspend fun getAnswerPageList(@Path("pageNo") pageNo: Int): Response<ArticleResponse>

  /**
   * 微信公众号列表
   */
  @GET("wxarticle/chapters/json")
  suspend fun getWxAuthors(): Response<List<WxArticle>>

  /**
   * 某个公众号下的文章列表
   */
  @GET("wxarticle/list/{id}/{page}/json")
  suspend fun getWxArticles(
    @Path("id") id: Int,
    @Path("page") page: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>
}