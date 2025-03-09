package com.sofar.take.picture.feature.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.base.app.BaseUIActivity
import com.sofar.mlkit.barcode.BarcodeScanActivity
import com.sofar.mlkit.core.MLKit
import com.sofar.take.picture.R
import com.sofar.take.picture.feature.login.LoginActivity

class SplashActivity : BaseUIActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    hideToolbar()
    LoginActivity.launch(this)
    finish()
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.splash_activity, viewGroup, false)
    return view
  }

}