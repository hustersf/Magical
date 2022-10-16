package com.sofar.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 添加一些通用配置,简化 WebView 的使用
 */
public class SofaWebView extends WebView {

  public SofaWebView(@NonNull Context context) {
    this(context, null);
  }

  public SofaWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SofaWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    WebSettings webSettings = getSettings();
    webSettings.setJavaScriptEnabled(true);

    setWebViewClient(mClient);
    setWebChromeClient(mChromeClient);
  }

  public void setWebViewClientProxy(@NonNull WebViewClient client) {
    mProxyClient = client;
  }

  public void setWebChromeClientProxy(@Nullable WebChromeClient client) {
    mProxyChromeClient = client;
  }

  private WebViewClient mProxyClient;
  private WebChromeClient mProxyChromeClient;

  private SofaWebViewClient mClient = new SofaWebViewClient();

  private SofaWebChromeClient mChromeClient = new SofaWebChromeClient() {
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
      super.onProgressChanged(view, newProgress);
      if (mProxyChromeClient != null) {
        mProxyChromeClient.onProgressChanged(view, newProgress);
      }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      super.onReceivedTitle(view, title);
      if (mProxyChromeClient != null) {
        mProxyChromeClient.onReceivedTitle(view, title);
      }
    }
  };
}
