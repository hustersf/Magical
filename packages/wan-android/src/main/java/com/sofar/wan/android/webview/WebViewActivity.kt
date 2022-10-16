package com.sofar.wan.android.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.sofar.base.BaseActivity
import com.sofar.wan.android.R
import com.sofar.webview.SofaWebView

class WebViewActivity : BaseActivity() {

  private var url: String? = null
  private lateinit var webView: SofaWebView
  private lateinit var titleView: TextView
  private lateinit var leftIcon: ImageView
  private lateinit var progressView: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.webview_activity)
    initView()
    initData()
    initWeb()
  }


  private fun initView() {
    webView = findViewById(R.id.web_view)
    titleView = findViewById(R.id.title)
    leftIcon = findViewById(R.id.left_icon)
    progressView = findViewById(R.id.web_progress)

    leftIcon.setOnClickListener {
      finish()
    }
  }

  private fun initData() {
    url = intent.getStringExtra(KEY_URL)
  }

  private fun initWeb() {
    webView.setWebChromeClientProxy(object : WebChromeClient() {
      override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        titleView.text = title
      }

      override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressView.progress = newProgress
        progressView.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
      }
    })
    webView.loadUrl(url ?: "")
  }


  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_BACK && back()) {
      return true
    }
    return super.onKeyDown(keyCode, event)
  }

  private fun back(): Boolean {
    if (webView.canGoBack()) {
      webView.goBack()
      return true
    }
    return false
  }

  companion object {
    private const val KEY_URL = "key_url"

    fun open(context: Context, url: String) {
      val intent = Intent(context, WebViewActivity::class.java)
      intent.putExtra(KEY_URL, url)
      context.startActivity(intent)
    }
  }

}