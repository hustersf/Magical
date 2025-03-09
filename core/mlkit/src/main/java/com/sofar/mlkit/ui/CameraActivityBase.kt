package com.sofar.mlkit.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.sofar.mlkit.core.GraphicOverlay
import com.sofar.mlkit.core.VisionImageProcessor
import java.util.concurrent.Executors

abstract class CameraActivityBase : AppCompatActivity() {

  private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
  private var cameraProvider: ProcessCameraProvider? = null
  private var imageProcessor: VisionImageProcessor? = null
  private var graphicOverlay: GraphicOverlay? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(layoutId())
    startCamera()
  }


  private fun startCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
      cameraProvider = cameraProviderFuture.get()
      bindCameraUseCases()
    }, ContextCompat.getMainExecutor(this))
  }

  private fun bindCameraUseCases() {
    if (cameraProvider == null) {
      return
    }
    cameraProvider?.unbindAll()
    graphicOverlay = graphicOverlay()
    imageProcessor = imageProcessor()
    val resolutionSelector = ResolutionSelector.Builder()
      .setAspectRatioStrategy(
        AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO)
      ).build()
    // 配置预览
    val preview = Preview.Builder()
      .build()
      .also { it.setSurfaceProvider(viewFinder().surfaceProvider) }

    // 配置图像分析
    val imageAnalysis = ImageAnalysis.Builder()
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .setResolutionSelector(resolutionSelector)
      .build()
      .also {
        it.setAnalyzer(cameraExecutor) { imageProxy ->
          imageProcessor?.processImageProxy(imageProxy, null)
        }
      }

    // 绑定用例
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    cameraProvider?.unbindAll()
    cameraProvider?.bindToLifecycle(
      this, cameraSelector, preview, imageAnalysis
    )
    onCameraBind()
  }

  abstract fun layoutId(): Int

  abstract fun onCameraBind()

  abstract fun viewFinder(): PreviewView

  abstract fun imageProcessor(): VisionImageProcessor

  fun graphicOverlay(): GraphicOverlay? {
    return null
  }

  override fun onResume() {
    super.onResume()
    bindCameraUseCases()
  }

  override fun onPause() {
    super.onPause()
    imageProcessor?.run { this.stop() }
  }

  override fun onDestroy() {
    super.onDestroy()
    cameraExecutor.shutdown()
    imageProcessor?.run { this.stop() }
  }
}