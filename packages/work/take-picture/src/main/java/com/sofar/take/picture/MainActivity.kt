package com.sofar.take.picture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.mlkit.barcode.BarcodeContract
import com.sofar.mlkit.core.MLKit
import com.sofar.take.picture.feature.daq.CapturePictureActivity
import com.sofar.take.picture.feature.daq.CaptureVideoActivity
import com.sofar.take.picture.feature.daq.PhotoActivity

class MainActivity : BaseUIActivity() {

  private lateinit var cameraBtn: Button
  private lateinit var videoBtn: Button
  private lateinit var photoBtn: Button
  private lateinit var scanBtn: Button

  private lateinit var scanLauncher: ActivityResultLauncher<Unit>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    initData()
    initObserve()
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.main_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    cameraBtn = findViewById(R.id.camera_btn)
    videoBtn = findViewById(R.id.video_btn)
    photoBtn = findViewById(R.id.photo_btn)
    scanBtn = findViewById(R.id.scan_btn)

    cameraBtn.setOnSingleClickListener {
      CapturePictureActivity.launch(this)
    }
    videoBtn.setOnSingleClickListener {
      CaptureVideoActivity.launch(this)
    }
    photoBtn.setOnSingleClickListener {
      PhotoActivity.launch(this)
    }
    scanBtn.setOnSingleClickListener {
      scanLauncher.launch(Unit)
    }
  }

  private fun initData() {
    setTitle(ContextCompat.getString(this, R.string.app_name))
    MLKit.get().preloadCameraX(this)
  }

  private fun initObserve() {
    scanLauncher = registerForActivityResult(BarcodeContract()) { result ->
      result?.let {
        Snackbar.make(scanBtn, result, Snackbar.LENGTH_SHORT).show()
      } ?: run {

      }
    }
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, MainActivity::class.java)
      context.startActivity(intent)
    }
  }
}