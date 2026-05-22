package com.sofar.core.ai.edge.data.storage

import android.content.Context
import android.os.StatFs
import java.io.File

object AppStorageHub {

  private const val DIR_NAME_MODELS = "models"

  fun modelsDir(context: Context): File {
    return File(context.getExternalFilesDir(null), DIR_NAME_MODELS)
  }

  data class StorageSnapshot(
    val modelsSize: Long,      // App所有模型和临时文件占用的总物理体积 (Byte)
    val availableSize: Long    // 当前外部存储分区的可用空间 (Byte)
  )

  fun getStorageSnapshot(context: Context): StorageSnapshot {
    val modelDir = modelsDir(context)
    val totalModelsSize = getFolderSize(modelDir)

    // 针对 getExternalFilesDir 所在的外部存储分区获取真实的可用空间
    val externalFolder = context.getExternalFilesDir(null)
    val availableSize = if (externalFolder != null) {
      val stat = StatFs(externalFolder.path)
      stat.availableBlocksLong * stat.blockSizeLong
    } else {
      0L
    }

    return StorageSnapshot(totalModelsSize, availableSize)
  }

  private fun getFolderSize(file: File): Long {
    if (!file.exists()) return 0L
    if (file.isFile) return file.length()
    var size = 0L
    file.listFiles()?.forEach { size += getFolderSize(it) }
    return size
  }
}
