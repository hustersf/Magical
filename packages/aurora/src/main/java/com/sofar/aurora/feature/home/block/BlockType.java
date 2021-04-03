package com.sofar.aurora.feature.home.block;

import java.util.HashMap;
import java.util.Map;

import com.sofar.aurora.feature.home.block.item.AlbumBlockItem;
import com.sofar.aurora.feature.home.block.item.ArtistBlockItem;
import com.sofar.aurora.feature.home.block.item.BannerBlockItem;
import com.sofar.aurora.feature.home.block.item.BlockItem;
import com.sofar.aurora.feature.home.block.item.CategoryBlockItem;
import com.sofar.aurora.feature.home.block.item.MenuBlockItem;
import com.sofar.aurora.feature.home.block.item.SongBlockItem;
import com.sofar.aurora.feature.home.block.item.TrackBlockItem;
import com.sofar.aurora.feature.home.block.item.VideoBlockItem;
import com.sofar.aurora.feature.home.model.HomeBlock;

public enum BlockType {

  TYPE_KEY_BANNER,
  TYPE_KEY_MENU,
  TYPE_KEY_ALBUM,
  TYPE_KEY_ARTIST,
  TYPE_KEY_VIDEO,
  TYPE_KEY_CATEGORY,
  TYPE_KEY_SONG,
  TYPE_KEY_TRACK,

  TYPE_UN_SUPPORT;

  public static BlockType getBlockType(HomeBlock block) {
    if (block == null) {
      return BlockType.TYPE_UN_SUPPORT;
    }

    switch (block.type) {
      case BlockTypeName.BANNER:
        return BlockType.TYPE_KEY_BANNER;
      case BlockTypeName.MENU:
        return BlockType.TYPE_KEY_MENU;
      case BlockTypeName.ALBUM:
        return BlockType.TYPE_KEY_ALBUM;
      case BlockTypeName.TRACK:
        return BlockType.TYPE_KEY_TRACK;
      case BlockTypeName.ARTIST:
        return BlockType.TYPE_KEY_ARTIST;
      case BlockTypeName.SONG:
        return BlockType.TYPE_KEY_SONG;
      case BlockTypeName.CATEGORY:
        return BlockType.TYPE_KEY_CATEGORY;
      case BlockTypeName.VIDEO:
        return BlockType.TYPE_KEY_VIDEO;
      default:
        return BlockType.TYPE_UN_SUPPORT;
    }
  }

  public static Map<BlockType, BlockItem> buildFullBlock() {
    Map<BlockType, BlockItem> map = new HashMap<>();
    map.put(BlockType.TYPE_KEY_BANNER, new BannerBlockItem());
    map.put(BlockType.TYPE_KEY_MENU, new MenuBlockItem());
    map.put(BlockType.TYPE_KEY_ALBUM, new AlbumBlockItem());
    map.put(BlockType.TYPE_KEY_TRACK, new TrackBlockItem());
    map.put(BlockType.TYPE_KEY_CATEGORY, new CategoryBlockItem());
    map.put(BlockType.TYPE_KEY_ARTIST, new ArtistBlockItem());
    map.put(BlockType.TYPE_KEY_SONG, new SongBlockItem());
    map.put(BlockType.TYPE_KEY_VIDEO, new VideoBlockItem());
    return map;
  }

}
