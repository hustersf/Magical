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

  override fun processBitmap(bitmap: Bitmap, graphicOverlay: GraphicOverlay?) {
    requestDetectInImage(
      InputImage.fromBitmap(bitmap, 0),
      graphicOverlay,
    )
  }

  override fun processByteBuffer(
    data: ByteBuffer,
    frameMetadata: FrameMetadata,
    graphicOverlay: GraphicOverlay?
  ) {

  }

  @OptIn(ExperimentalGetImage::class)
  override fun processImageProxy(imageProxy: ImageProxy, graphicOverlay: GraphicOverlay?) {
    if (imageProxy.image != null) {
      imageProxy.image?.let {
        requestDetectInImage(
          InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees),
          graphicOverlay
        )
          .addOnCompleteListener {
            imageProxy.close()
          }
      }
    } else {
      imageProxy.close()
    }
  }

  private fun requestDetectInImage(
    image: InputImage,
    graphicOverlay: GraphicOverlay?
  ): Task<T> {
    return setUpListener(detectInImage(image), graphicOverlay)
  }

  private fun setUpListener(
    task: Task<T>,
    graphicOverlay: GraphicOverlay?,

    ): Task<T> {
    return task
      .addOnSuccessListener { results: T ->
        onSuccess(results, graphicOverlay)
        graphicOverlay?.postInvalidate()
      }
      .addOnFailureListener { e: Exception ->
        onFailure(e)
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