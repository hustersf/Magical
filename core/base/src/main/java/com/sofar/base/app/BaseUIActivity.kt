package com.sofar.base.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.sofar.base.R

/**
 * 公共UI的集合,todo 逐步扩展
 *
 * 页面结构
 * 1.沉浸式状态栏
 * 2.标题
 * 3.子页面布局
 */
abstract class BaseUIActivity : AppCompatActivity() {

  private lateinit var rootView: ViewGroup
  private lateinit var contentView: ViewGroup
  private lateinit var toolbar: MaterialToolbar
  private lateinit var loadingView: LinearProgressIndicator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_base_ui)
    rootView = findViewById(R.id.base_root)
    contentView = findViewById(R.id.base_content)
    toolbar = findViewById(R.id.base_toolbar)
    loadingView = findViewById(R.id.base_loading)
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }
    contentView.addView(onCreateView(LayoutInflater.from(this), contentView, savedInstanceState))
  }

  abstract fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View

  fun setTitle(text: String) {
    toolbar.visibility = View.VISIBLE
    toolbar.title = text
  }

  fun hideToolbar() {
    toolbar.visibility = View.GONE
  }

  fun showLoading() {
    loadingView.visibility = View.VISIBLE
  }

  fun hideLoading() {
    loadingView.visibility = View.GONE
  }


  override fun onDestroy() {
    super.onDestroy()
  }
}