package com.sofar.snapu.feature.daq

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.snapu.R

class CaptureVideoActivity : BaseUIActivity() {

  private lateinit var loginBtn: Button

  private lateinit var videoUri: Uri
  private lateinit var captureVideoLauncher: ActivityResultLauncher<Uri>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    captureVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo())
    { result ->
    }
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.photo_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    loginBtn = findViewById(R.id.login_btn)
    loginBtn.setOnSingleClickListener {
      captureVideo()
    }
  }

  private fun captureVideo() {
    val name = "video_${System.currentTimeMillis()}.mp4"
    videoUri = CaptureFileUtil.getVideoUri(this, name)
    captureVideoLauncher.launch(videoUri)
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, CaptureVideoActivity::class.java)
      context.startActivity(intent)
    }
  }
}