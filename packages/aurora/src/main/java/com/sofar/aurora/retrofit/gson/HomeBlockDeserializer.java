package com.sofar.aurora.retrofit.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sofar.aurora.feature.home.block.BlockTypeName;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Album;
import com.sofar.aurora.model.Artist;
import com.sofar.aurora.model.Banner;
import com.sofar.aurora.model.Category;
import com.sofar.aurora.model.Menu;
import com.sofar.aurora.model.Song;
import com.sofar.aurora.model.Track;
import com.sofar.aurora.model.Video;
import com.sofar.base.util.JsonUtil;

public class HomeBlockDeserializer implements JsonDeserializer<HomeBlock> {

  @Override
  public HomeBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
    throws JsonParseException {
    JsonObject obj = (JsonObject) json;
    HomeBlock block = new HomeBlock();
    block.type = JsonUtil.optString(obj, "type", "");
    block.name = JsonUtil.optString(obj, "module_name", "");
    block.haveMore = JsonUtil.optInt(obj, "haveMore", 0);
    JsonArray results = obj.get("result").getAsJsonArray();

    switch (block.type) {
      case BlockTypeName.BANNER:
        List<Banner> banners =
          Gsons.GSON.fromJson(results, new TypeToken<List<Banner>>() {}.getType());
        block.results = banners;
        break;
      case BlockTypeName.MENU:
        List<Menu> menus = Gsons.GSON.fromJson(results, new TypeToken<List<Menu>>() {}.getType());
        block.results = menus;
        break;
      case BlockTypeName.ALBUM:
        List<Album> albums =
          Gsons.GSON.fromJson(results, new TypeToken<List<Album>>() {}.getType());
        block.results = albums;
        break;
      case BlockTypeName.TRACK:
        List<Track> tracks =
          Gsons.GSON.fromJson(results, new TypeToken<List<Track>>() {}.getType());
        block.results = tracks;
        break;
      case BlockTypeName.ARTIST:
        List<Artist> artists =
          Gsons.GSON.fromJson(results, new TypeToken<List<Artist>>() {}.getType());
        block.results = artists;
        break;
      case BlockTypeName.SONG:
        List<Song> songs = Gsons.GSON.fromJson(results, new TypeToken<List<Song>>() {}.getType());
        block.results = songs;
        break;
      case BlockTypeName.CATEGORY:
        List<Category> categories =
          Gsons.GSON.fromJson(results, new TypeToken<List<Category>>() {}.getType());
        block.results = categories;
        break;
      case BlockTypeName.VIDEO:
        List<Video> videos =
          Gsons.GSON.fromJson(results, new TypeToken<List<Video>>() {}.getType());
        block.results = videos;
        break;
      default:
        block.results = new ArrayList();
        break;
    }
    return block;
  }

}
