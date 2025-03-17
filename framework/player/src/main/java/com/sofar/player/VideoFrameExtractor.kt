package com.sofar.player

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever


object VideoFrameExtractor {

  // 提取帧（异步回调）
  fun extractFrameAsync(videoPath: String, timeUs: Long, listener: OnFrameExtractedListener?) {
    Thread {
      val frame = extractFrameAtTime(videoPath, timeUs)
      listener?.onFrameExtracted(frame)
    }.start()
  }

  // 同步提取帧
  fun extractFrameAtTime(videoPath: String, timeUs: Long): Bitmap? {
    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(videoPath)
      return retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    } finally {
      retriever.release()
    }
  }

  interface OnFrameExtractedListener {
    fun onFrameExtracted(bitmap: Bitmap?)
  }
}