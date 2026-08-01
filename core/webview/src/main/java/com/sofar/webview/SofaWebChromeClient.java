package com.sofar.webview;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class SofaWebChromeClient extends WebChromeClient {

  @Override
  public void onProgressChanged(WebView view, int newProgress) {
    super.onProgressChanged(view, newProgress);
    Log.d(Const.TAG, "onProgressChanged:" + newProgress);
  }

  @Override
  public void onReceivedTitle(WebView view, String title) {
    super.onReceivedTitle(view, title);
    Log.d(Const.TAG, "onReceivedTitle:" + title);
  }
}
