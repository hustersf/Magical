package com.sofar.webview;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SofaWebViewClient extends WebViewClient {

  public static final String HTTP_SCHEME = "http://";
  public static final String HTTPS_SCHEME = "https://";

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

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    String url = request.getUrl().toString();
    Log.d(Const.TAG, "shouldOverrideUrlLoading:" + url);
    if (url.startsWith(HTTP_SCHEME) || url.startsWith(HTTPS_SCHEME)) {
      return false;
    }
    //拦截不认识的url
    return true;
  }
}
