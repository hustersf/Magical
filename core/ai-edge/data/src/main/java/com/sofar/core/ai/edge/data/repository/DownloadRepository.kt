package com.sofar.core.ai.edge.data.repository

import android.content.Context
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatusType
import com.sofar.core.download.DownloadManager
import com.sofar.core.download.ZipUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class DownloadRepository {

  private val downloadManager = DownloadManager()

  fun downloadModel(context: Context, model: Model): Flow<ModelDownloadStatus> = callbackFlow {
    val targetFile = File(model.getPath(context))
    val tmpFile = File(model.getTmpPath(context))
    targetFile.parentFile?.mkdirs()
    tmpFile.parentFile?.mkdirs()

    try {
      downloadManager.download(
        fileUrl = model.url,
        targetFile = targetFile,
        totalBytes = model.sizeInBytes,
        tmpFile = tmpFile
      ) { downloaded, rate, remainingMs ->

        val progressStatus = ModelDownloadStatus(
          statusType = ModelDownloadStatusType.IN_PROGRESS,
          totalBytes = model.sizeInBytes,
          receivedBytes = downloaded,
          bytesPerSecond = rate,
          remainingMs = remainingMs
        )

        trySend(progressStatus)
      }

      // 下载完成后的后续逻辑，在这里可以直接用 send() 发射状态
      if (model.isZip && model.unzipDir.isNotEmpty()) {
        send(ModelDownloadStatus(ModelDownloadStatusType.UNZIPPING))
        // 磁盘解压属于耗时操作，如果 ZipUtil 没有自带切线程，这里用 withContext 保护一下主线程
        withContext(Dispatchers.IO) {
          ZipUtil.unzip(targetFile, File(model.getPath(context)))
        }
        if (targetFile.exists()) targetFile.delete()
      }

      // 最终通关发射
      send(ModelDownloadStatus(ModelDownloadStatusType.SUCCEEDED))
      channel.close()

    } catch (e: Exception) {
      // 拦截任何异常投递给前端，防止崩溃
      send(
        ModelDownloadStatus(
          statusType = ModelDownloadStatusType.FAILED,
          errorMessage = e.localizedMessage ?: "网络同步遭遇异常"
        )
      )
      channel.close()
    }
    awaitClose {
      // 如果你的 downloadManager 支持中途取消，可以在这里调用：downloadManager.cancel()
    }
  }.flowOn(Dispatchers.IO)
}