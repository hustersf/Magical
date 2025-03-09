package com.sofar.take.picture.feature.daq

import android.content.Context
import android.content.Intent
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

class PhotoActivity : BaseUIActivity() {

  private lateinit var loginBtn: Button

  private var mimeType = "*/*"

  private lateinit var getContentLauncher: ActivityResultLauncher<String>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent())
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
      openPhoto()
    }
  }

  private fun openPhoto() {
    getContentLauncher.launch(mimeType)
  }


  companion object {
    fun launch(context: Context) {
      val intent = Intent(context, PhotoActivity::class.java)
      context.startActivity(intent)
    }
  }
}