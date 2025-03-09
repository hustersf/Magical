package com.sofar.take.picture.feature.daq

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
import com.sofar.take.picture.R

class CapturePictureActivity : BaseUIActivity() {

  private lateinit var loginBtn: Button

  private lateinit var imageUri: Uri
  private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture())
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
      captureImage()
    }
  }

  private fun captureImage() {
    val name = "photo_${System.currentTimeMillis()}.png"
    imageUri = CaptureFileUtil.getPhotoUri(this, name)
    takePictureLauncher.launch(imageUri)
  }

  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, CapturePictureActivity::class.java)
      context.startActivity(intent)
    }
  }
}