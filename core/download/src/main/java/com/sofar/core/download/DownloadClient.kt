package com.sofar.core.download

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 下载执行器：纯阻塞式 HTTP 下载（断点续传、测速、重命名）。
 */
class DownloadClient(private val accessToken: String? = null) {
  companion object {
    private const val TAG = "DownloadClient"
    private const val DEFAULT_BUFFER_SIZE = 8192
    private const val UNKNOWN_TOTAL_BYTES = -1L
  }


  /**
   * 下载方法：涵盖断点续传、测速、重命名
   */
  fun download(
    fileUrl: String,
    targetFile: File,
    totalBytes: Long = UNKNOWN_TOTAL_BYTES,
    tmpFile: File? = null,
    onProgress: (downloaded: Long, totalBytes: Long, rate: Long, remainingMs: Long) -> Unit
  ) {
    val finalWriteFile = tmpFile ?: targetFile
    val url = URL(fileUrl)
    val connection = url.openConnection() as HttpURLConnection

    accessToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }

    // 局部化变量，确保多任务并发数据隔离
    var downloadedBytes = 0L
    val bytesReadSizeBuffer = mutableListOf<Long>()
    val bytesReadLatencyBuffer = mutableListOf<Long>()

    // 断点续传准备
    val outputFileBytes = finalWriteFile.length()
    if (outputFileBytes > 0) {
      Log.d(
        TAG,
        "File '${finalWriteFile.name}' partial size: ${outputFileBytes}. Trying to resume download"
      )
      connection.setRequestProperty("Range", "bytes=${outputFileBytes}-")
      connection.setRequestProperty("Accept-Encoding", "identity")
    }

    connection.connect()
    Log.d(TAG, "response code: ${connection.responseCode}")

    // 校验响应与偏移计算
    if (connection.responseCode == HttpURLConnection.HTTP_PARTIAL) {
      val contentRange = connection.getHeaderField("Content-Range")
      if (contentRange != null) {
        val rangeParts = contentRange.substringAfter("bytes ").split("/")
        val byteRange = rangeParts[0].split("-")
        val startByte = byteRange[0].toLong()

        // 直接赋值对齐断点
        downloadedBytes = startByte
      }
    } else if (connection.responseCode == HttpURLConnection.HTTP_OK) {
      // 不支持续传则清理旧文件
      if (finalWriteFile.exists()) {
        finalWriteFile.delete()
      }
      downloadedBytes = 0L
    } else {
      throw IOException("HTTP error code: ${connection.responseCode}")
    }

    val resolvedTotalBytes = resolveTotalBytes(
      connection = connection,
      responseCode = connection.responseCode,
      outputFileBytes = outputFileBytes,
      requestedTotalBytes = totalBytes
    )

    Log.d(TAG, "resolvedTotalBytes: $resolvedTotalBytes")

    try {
      // 流读写与测速逻辑
      connection.inputStream.use { input ->
        FileOutputStream(finalWriteFile, true).use { output ->
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
                val bytesPerMs = calculateSpeed(
                  deltaBytes,
                  curTs - lastSetProgressTs,
                  bytesReadSizeBuffer,
                  bytesReadLatencyBuffer
                )
                deltaBytes = 0L

                val remainingMs = if (bytesPerMs > 0f && resolvedTotalBytes > 0L) {
                  ((resolvedTotalBytes - downloadedBytes).coerceAtLeast(0L) / bytesPerMs).toLong()
                } else 0L

                val currentSpeedBytesPerSec = (bytesPerMs * 1000).toLong()

                onProgress(
                  downloadedBytes,
                  resolvedTotalBytes,
                  currentSpeedBytesPerSec,
                  remainingMs
                )
              }
              lastSetProgressTs = curTs
            }
          }
        }
      }

      // 重命名
      if (finalWriteFile != targetFile) {
        if (targetFile.exists()) targetFile.delete()
        if (!finalWriteFile.renameTo(targetFile)) throw IOException("Rename failed")
        Log.d(TAG, "Download done")
      }
    } finally {
      connection.disconnect()
    }
  }

  // 测速算法平滑处理
  private fun calculateSpeed(
    deltaBytes: Long,
    deltaTime: Long,
    sizeBuffer: MutableList<Long>,
    latencyBuffer: MutableList<Long>
  ): Float {
    if (sizeBuffer.size == 5) {
      sizeBuffer.removeAt(0)
    }
    sizeBuffer.add(deltaBytes)
    if (latencyBuffer.size == 5) {
      latencyBuffer.removeAt(0)
    }
    latencyBuffer.add(deltaTime)
    return sizeBuffer.sum().toFloat() / latencyBuffer.sum()
  }

  private fun resolveTotalBytes(
    connection: HttpURLConnection,
    responseCode: Int,
    outputFileBytes: Long,
    requestedTotalBytes: Long
  ): Long {
    if (requestedTotalBytes > 0L) {
      return requestedTotalBytes
    }

    val contentLength = connection.getHeaderFieldLong("Content-Length", UNKNOWN_TOTAL_BYTES)
    if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
      val contentRange = connection.getHeaderField("Content-Range")
      val totalFromContentRange = parseTotalBytesFromContentRange(contentRange)
      if (totalFromContentRange > 0L) {
        return totalFromContentRange
      }

      if (contentLength > 0L) {
        return outputFileBytes + contentLength
      }
    } else if (contentLength > 0L) {
      return contentLength
    }

    return UNKNOWN_TOTAL_BYTES
  }

  private fun parseTotalBytesFromContentRange(contentRange: String?): Long {
    if (contentRange.isNullOrBlank()) {
      return UNKNOWN_TOTAL_BYTES
    }

    val totalPart = contentRange.substringAfterLast('/', missingDelimiterValue = "")
    return totalPart.toLongOrNull()?.takeIf { it > 0L } ?: UNKNOWN_TOTAL_BYTES
  }
}
