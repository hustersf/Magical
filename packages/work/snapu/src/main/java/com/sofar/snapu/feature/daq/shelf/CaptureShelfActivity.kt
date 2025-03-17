package com.sofar.snapu.feature.daq.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.base.app.BaseUIActivity
import com.sofar.snapu.R

class CaptureShelfActivity : BaseUIActivity() {

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.capture_shelf_activity, viewGroup, false)
    return view
  }
}