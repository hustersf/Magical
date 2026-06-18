package com.sofar.core.ai.edge.database

import androidx.room.TypeConverter

class RoomConverters {
  @TypeConverter
  fun fromStringList(value: List<String>?): String? {
    // 存入数据库：将 ["path1", "path2"] 变成 "path1,path2"
    return value?.joinToString(separator = ",")
  }

  @TypeConverter
  fun toStringList(value: String?): List<String>? {
    // 从数据库读取：将 "path1,path2" 还原为 ["path1", "path2"]
    return value?.split(",")?.filter { it.isNotBlank() }
  }
}