package com.sofar.wan.android.feature.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sofar.wan.android.R

open class SimplePageStateUI(val parent: ViewGroup) : PageStateUI(parent) {

  private val loadView by lazy { createLoadingView() }

  private val emptyView by lazy { createEmptyView() }

  private val errorView by lazy { createErrorView() }

  fun showLoading() {
    showStateView(loadView)
  }

  fun hideLoading() {
    hideStateView()
  }

  open fun createLoadingView(): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.state_loading_layout, null)
  }

  fun showEmpty() {
    showStateView(emptyView)
  }

  fun hideEmpty() {
    hideStateView()
  }


  open fun createEmptyView(): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.state_empty_layout, null)
  }

  fun showError() {
    showStateView(errorView)
  }

  fun hideError() {
    hideStateView()
  }

  open fun createErrorView(): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.state_error_layout, null)
  }
}