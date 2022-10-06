package com.sofar.wan.android.network.api

import com.sofar.wan.android.model.Article
import com.sofar.wan.android.model.Banner
import com.sofar.wan.android.model.Kind
import com.sofar.wan.android.model.Navigation
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
  suspend fun getWxAuthors(): Response<List<Kind>>

  /**
   * 某个公众号下的文章列表
   */
  @GET("wxarticle/list/{id}/{page}/json")
  suspend fun getWxArticles(
    @Path("id") id: Int,
    @Path("page") page: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>

  /**
   * 项目分类
   */
  @GET("project/tree/json")
  suspend fun getProjectKind(): Response<List<Kind>>

  /**
   * 某个项目分类下的项目列表
   */
  @GET("wxarticle/list/{id}/{page}/json")
  suspend fun getProjectKindArticles(
    @Path("id") id: Int,
    @Path("page") page: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>

  /**
   * 最新项目列表
   */
  @GET("article/listproject/{page}/json")
  suspend fun getNewProjectArticles(
    @Path("page") page: Int,
    @Query("page_size") pageSize: Int,
  ): Response<ArticleResponse>

  /**
   * 获取导航列表
   */
  @GET("navi/json")
  suspend fun getNavigationList(): Response<List<Navigation>>

  /**
   * 获取体系类别
   */
  @GET("tree/json")
  suspend fun getCategoryList(): Response<List<Kind>>

  /**
   * 获取体系类别下的文章列表
   */
  @GET("article/list/{page}/json")
  suspend fun getCategoryArticles(
    @Path("page") page: Int,
    @Query("cid") id: Int,
  ): Response<ArticleResponse>

  /**
   * 获取教程列表
   */
  @GET("chapter/547/sublist/json")
  suspend fun getCourseList(): Response<List<Kind>>

  /**
   * 获取某个教程下的文章
   */
  @GET("article/list/{page}/json")
  suspend fun getCourseArticles(
    @Path("page") page: Int,
    @Query("cid") id: Int,
    @Query("order_type") orderType: Int = 1,
  ): Response<ArticleResponse>
}