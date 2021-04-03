package com.sofar.aurora.retrofit.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sofar.aurora.feature.home.model.HomeBlock;

public class Gsons {

  public static final Gson GSON = new Gson();

  /**
   * {
   * "status": 1,
   * "msg": "message",
   * "data":{}
   * }
   * 标准后台数据，统一解析
   * <p>
   * 也可以利用registerTypeAdapter 自己把控 一个实体类的解析,
   * 无需利用@SerializedName配合GsonConverterFactory来解析
   */
  public static final Gson STANDARD_GSON = new GsonBuilder()
    .registerTypeAdapter(Response.class, new ResponseDeserializer())
    .registerTypeAdapter(Response.class, new ResponseSerializer())
    .registerTypeAdapter(HomeBlock.class, new HomeBlockDeserializer())
    .create();

}
