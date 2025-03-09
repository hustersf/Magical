package com.sofar.mlkit.core

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

abstract class VisionProcessorBase<T>(context: Context) : VisionImageProcessor {

  private var isShutdown = false

  private val fpsTimer = Timer()
  private var frameProcessedInOneSecondInterval = 0
  private var framesPerSecond = 0

  init {
    fpsTimer.schedule(
      object : TimerTask() {
        override fun run() {
          framesPerSecond = frameProcessedInOneSecondInterval
          frameProcessedInOneSecondInterval = 0
        }
      },
      0,
      1000
    )
  }

  override fun processBitmap(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {

  }

  override fun processByteBuffer(
    data: ByteBuffer,
    frameMetadata: FrameMetadata,
    graphicOverlay: GraphicOverlay
  ) {

  }

  @OptIn(ExperimentalGetImage::class)
  override fun processImageProxy(imageProxy: ImageProxy, graphicOverlay: GraphicOverlay?) {
    if (imageProxy.image != null) {
      imageProxy.image?.let {
        detectInImage(InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees))
          .addOnSuccessListener { results: T ->
            onSuccess(results, graphicOverlay)
          }
          .addOnFailureListener { e: Exception ->
            onFailure(e)
          }
          .addOnCompleteListener {
            imageProxy.close()
          }
      }
    } else {
      imageProxy.close()
    }
  }

  override fun stop() {
    isShutdown = true
    resetLatencyStats()
    fpsTimer.cancel()
  }

  private fun resetLatencyStats() {
  }

  /**
   * 裁剪待分析的图片
   */
  open fun cropImage(imageProxy: ImageProxy) {
  }

  protected abstract fun detectInImage(image: InputImage): Task<T>

  protected abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay?)

  protected abstract fun onFailure(e: Exception)
}