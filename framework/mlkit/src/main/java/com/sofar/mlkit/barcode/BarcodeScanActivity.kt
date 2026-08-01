package com.sofar.mlkit.barcode

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.barcode.common.Barcode
import com.sofar.mlkit.R
import com.sofar.mlkit.Util
import com.sofar.mlkit.core.VisionImageProcessor
import com.sofar.mlkit.ui.CameraActivityBase

class BarcodeScanActivity : CameraActivityBase() {

  private lateinit var viewFinder: PreviewView
  private lateinit var scanView: BarcodeScanView

  private var handler: Handler = Handler(Looper.getMainLooper())
  private var barcodeResult = object : BarcodeResult {
    override fun success(result: String) {
      handler.post {
        showResult(result)
      }
    }

    override fun error(e: Exception) {

    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewFinder = findViewById(R.id.preview_view)
    scanView = findViewById(R.id.scan_view)
  }

  override fun layoutId(): Int {
    return R.layout.sacnner_activity
  }

  override fun onCameraBind() {

  }

  override fun viewFinder(): PreviewView {
    return viewFinder
  }

  override fun imageProcessor(): VisionImageProcessor {
    val scannerProcessor = BarcodeScannerProcessor(this, barcodeResult)
    scannerProcessor.setAnalysisArea(scanView.getAnalysisArea())
    return scannerProcessor
  }

  private fun showResult(result: String) {
    Util.vibrate(this, 200L)
    setResult(RESULT_OK, Intent().apply {
      putExtra(BarcodeContract.KEY_SCAN_RESULT, result)
    })
    finish()
  }


  override fun onDestroy() {
    super.onDestroy()
    handler.removeCallbacksAndMessages(null)
  }
}