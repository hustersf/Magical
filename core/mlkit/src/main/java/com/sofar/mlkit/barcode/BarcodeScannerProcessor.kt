package com.sofar.mlkit.barcode

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.util.component1
import androidx.core.util.component2
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.sofar.mlkit.core.GraphicOverlay
import com.sofar.mlkit.core.VisionProcessorBase

class BarcodeScannerProcessor(context: Context, private var result: BarcodeResult? = null) :
  VisionProcessorBase<List<Barcode>>(context) {

  private var analysisArea: RectF? = null

  private val barcodeScanner: BarcodeScanner by lazy {
    val options = BarcodeScannerOptions.Builder()
      .setBarcodeFormats(
        Barcode.FORMAT_EAN_13,  // 商品条码
        Barcode.FORMAT_QR_CODE  // 二维码
      ).build()
    BarcodeScanning.getClient(options)
  }

  fun setAnalysisArea(rectF: RectF) {
    analysisArea = rectF
  }

  override fun onSuccess(results: List<Barcode>, graphicOverlay: GraphicOverlay?) {
    result?.success(results)
  }

  override fun onFailure(e: Exception) {
    result?.error(e)
  }

  override fun stop() {
    super.stop()
    barcodeScanner.close()
  }

  @OptIn(ExperimentalGetImage::class)
  override fun cropImage(imageProxy: ImageProxy) {
    analysisArea?.let {
      // 获取设备旋转信息
      val rotation = imageProxy.imageInfo.rotationDegrees

      // 转换坐标到图像坐标系
      val viewArea = it
      val imageRect = mapViewCoordinatesToImage(
        viewArea,
        Size(imageProxy.width, imageProxy.height),
        rotation
      )
      imageProxy.image?.setCropRect(imageRect)
    }
  }

  override fun detectInImage(image: InputImage): Task<List<Barcode>> {
    return barcodeScanner.process(image)
  }

  private fun mapViewCoordinatesToImage(
    viewRect: RectF,
    imageSize: Size,
    sensorRotation: Int
  ): Rect {
    val (rotatedWidth, rotatedHeight) = when (sensorRotation) {
      90, 270 -> Size(imageSize.height, imageSize.width)
      else -> imageSize
    }

    return Rect(
      (rotatedWidth * viewRect.left).toInt(),
      (rotatedHeight * viewRect.top).toInt(),
      (rotatedWidth * viewRect.right).toInt(),
      (rotatedHeight * viewRect.bottom).toInt()
    )
  }

}