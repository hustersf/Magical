package com.sofar.webview;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SofaWebViewClient extends WebViewClient {

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    super.onPageStarted(view, url, favicon);
    Log.d(Const.TAG, "onPageStarted:" + url);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    Log.d(Const.TAG, "onPageFinished:" + url);
  }
}
