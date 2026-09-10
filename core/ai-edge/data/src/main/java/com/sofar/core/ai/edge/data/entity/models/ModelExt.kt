package com.sofar.core.ai.edge.data.entity.models

import android.content.Context
import com.sofar.core.ai.edge.data.storage.AppStorageHub
import java.io.File

/**
 * 存放 Model 的平台相关扩展函数（如路径计算、物理状态检查）
 * 这样可以保持 Model 类本身的纯净，同时保留调用的便捷性
 */

fun Model.getBaseDir(context: Context): String {
  return listOf(
    AppStorageHub.modelsDir(context).absolutePath,
    normalizedName,
    version
  ).joinToString(File.separator)
}

fun Model.getPath(context: Context, fileName: String = downloadFileName): String {
  val baseDir = getBaseDir(context)
  return if (isZip && unzipDir.isNotEmpty()) {
    listOf(baseDir, unzipDir).joinToString(File.separator)
  } else {
    listOf(baseDir, fileName).joinToString(File.separator)
  }
}

fun Model.getTmpPath(context: Context): String {
  val ext = "tmp"
  return getPath(context, fileName = "$downloadFileName.$ext")
}

fun Model.getDownloadStatus(context: Context): ModelDownloadStatus {
  val fileOrDir = File(getPath(context))
  val tmpFile = File(getTmpPath(context))

  return when {
    fileOrDir.exists() && fileOrDir.isFile && fileOrDir.length() == sizeInBytes -> {
      ModelDownloadStatus(statusType = ModelDownloadStatusType.SUCCEEDED)
    }

    tmpFile.exists() && tmpFile.isFile && tmpFile.length() > 0 -> {
      ModelDownloadStatus(
        statusType = ModelDownloadStatusType.PARTIALLY_DOWNLOADED,
        totalBytes = this.sizeInBytes,
        receivedBytes = tmpFile.length()
      )
    }

    else -> {
      ModelDownloadStatus(statusType = ModelDownloadStatusType.NOT_DOWNLOADED)
    }
  }
}

fun Model.deleteModelFile(context: Context) {
  val modelFile = File(getPath(context))
  if (modelFile.exists()) {
    modelFile.delete()
  }
}
