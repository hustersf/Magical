package com.sofar.snapu.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.base.app.BaseUIActivity
import com.sofar.snapu.R
import com.sofar.snapu.feature.login.LoginActivity

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