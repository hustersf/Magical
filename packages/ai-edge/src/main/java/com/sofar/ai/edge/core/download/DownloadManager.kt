package com.sofar.ai.edge.core.download

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "DownloadManager"
private const val TMP_FILE_EXT = "tmp"
private const val DEFAULT_BUFFER_SIZE = 8 * 1024

class DownloadManager(private val accessToken: String? = null) {
  // 成员变量：跨文件累加进度和测速采样
  private var downloadedBytes = 0L
  private val bytesReadSizeBuffer = mutableListOf<Long>()
  private val bytesReadLatencyBuffer = mutableListOf<Long>()

  /**
   * 下载方法：涵盖断点续传、测速、重命名
   */
  suspend fun download(
    fileUrl: String,
    targetFile: File,
    totalBytes: Long,
    onProgress: (downloaded: Long, rate: Long, remainingMs: Long) -> Unit
  ) = withContext(Dispatchers.IO) {
    val tmpFile = File("${targetFile.absolutePath}.$TMP_FILE_EXT")
    val url = URL(fileUrl)
    val connection = url.openConnection() as HttpURLConnection

    accessToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }

    // 断点续传准备
    val outputFileBytes = tmpFile.length()
    if (outputFileBytes > 0) {
      Log.d(
        TAG,
        "File '${tmpFile.name}' partial size: ${outputFileBytes}. Trying to resume download"
      )
      connection.setRequestProperty("Range", "bytes=${outputFileBytes}-")
      connection.setRequestProperty("Accept-Encoding", "identity")
    }

    connection.connect()
    Log.d(TAG, "response code: ${connection.responseCode}")

    // 校验响应与偏移计算
    if (connection.responseCode == HttpURLConnection.HTTP_OK ||
      connection.responseCode == HttpURLConnection.HTTP_PARTIAL
    ) {
      val contentRange = connection.getHeaderField("Content-Range")
      if (contentRange != null) {
        val rangeParts = contentRange.substringAfter("bytes ").split("/")
        val byteRange = rangeParts[0].split("-")
        val startByte = byteRange[0].toLong()
        val endByte = byteRange[1].toLong()

        Log.d(
          TAG,
          "Content-Range: $contentRange. Start bytes: ${startByte}, end bytes: $endByte",
        )

        downloadedBytes += startByte
      } else {
        Log.d(TAG, "Download starts from beginning.")
      }
    } else {
      throw IOException("HTTP error code: ${connection.responseCode}")
    }

    // 流读写与测速逻辑
    connection.inputStream.use { input ->
      FileOutputStream(tmpFile, true).use { output ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int
        var lastSetProgressTs: Long = 0
        var deltaBytes = 0L

        while (input.read(buffer).also { bytesRead = it } != -1) {
          output.write(buffer, 0, bytesRead)
          downloadedBytes += bytesRead
          deltaBytes += bytesRead

          val curTs = System.currentTimeMillis()
          if (curTs - lastSetProgressTs > 200) {
            if (lastSetProgressTs != 0L) {
              val bytesPerMs = calculateSpeed(deltaBytes, curTs - lastSetProgressTs)
              deltaBytes = 0L

              val remainingMs = if (bytesPerMs > 0f && totalBytes > 0L) {
                ((totalBytes - downloadedBytes) / bytesPerMs).toLong()
              } else 0L

              onProgress(downloadedBytes, (bytesPerMs * 1000).toLong(), remainingMs)
            }
            Log.d(TAG, "downloadedBytes: $downloadedBytes")
            lastSetProgressTs = curTs
          }
        }
      }
    }

    // 重命名
    if (targetFile.exists()) targetFile.delete()
    if (!tmpFile.renameTo(targetFile)) throw IOException("Rename failed")
    Log.d(TAG, "Download done")
  }

  private fun calculateSpeed(deltaBytes: Long, deltaTime: Long): Float {
    if (bytesReadSizeBuffer.size == 5) {
      bytesReadSizeBuffer.removeAt(0)
    }
    bytesReadSizeBuffer.add(deltaBytes)
    if (bytesReadLatencyBuffer.size == 5) {
      bytesReadLatencyBuffer.removeAt(0)
    }
    bytesReadLatencyBuffer.add(deltaTime)
    return bytesReadSizeBuffer.sum().toFloat() / bytesReadLatencyBuffer.sum()
  }
}
