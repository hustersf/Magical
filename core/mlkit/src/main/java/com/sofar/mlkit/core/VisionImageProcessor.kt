package com.sofar.mlkit.core

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException
import java.nio.ByteBuffer

interface VisionImageProcessor {

  fun processBitmap(bitmap: Bitmap, graphicOverlay: GraphicOverlay? = null)

  @Throws(MlKitException::class)
  fun processByteBuffer(
    data: ByteBuffer,
    frameMetadata: FrameMetadata,
    graphicOverlay: GraphicOverlay? = null
  )

  @Throws(MlKitException::class)
  fun processImageProxy(imageProxy: ImageProxy, graphicOverlay: GraphicOverlay? = null)

  fun stop()
}