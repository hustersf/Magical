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
      downloadManager.await(
        request = DownloadManager.DownloadRequest(
          fileUrl = model.url,
          targetFile = targetFile,
          totalBytes = model.sizeInBytes,
          tmpFile = tmpFile,
        ),
      ) { progress ->
        val progressStatus = ModelDownloadStatus(
          statusType = ModelDownloadStatusType.IN_PROGRESS,
          totalBytes = progress.totalBytes,
          receivedBytes = progress.downloadedBytes,
          bytesPerSecond = progress.speedBytesPerSec,
          remainingMs = progress.remainingMs,
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
          errorMessage = e.localizedMessage ?: "网络同步遭遇异常",
        ),
      )
      channel.close()
    }
    awaitClose {
      // 协调器模式下，调用方销毁不主动 cancel，后续订阅者可复用同一下载任务
    }
  }.flowOn(Dispatchers.IO)
}