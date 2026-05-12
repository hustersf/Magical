package com.sofar.core.ui

import android.R
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

abstract class BaseUIActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
  }

  override fun setContentView(layoutResID: Int) {
    super.setContentView(layoutResID)
    applyInsetsToRoot()
  }

  override fun setContentView(view: View?) {
    super.setContentView(view)
    applyInsetsToRoot()
  }

  override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
    super.setContentView(view, params)
    applyInsetsToRoot()
  }

  private fun applyInsetsToRoot() {
    val contentContainer = findViewById<ViewGroup>(R.id.content) ?: return
    val rootView = contentContainer.getChildAt(0) ?: return
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
      val systemBars = insets.getInsets(windowInsetsType())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
  }

  open fun windowInsetsType(): Int {
    return WindowInsetsCompat.Type.systemBars()
  }
}