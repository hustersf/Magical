package com.sofar.snapu.widget.page

import android.view.View
import android.view.ViewGroup
import com.sofar.snapu.R
import com.sofar.utility.ViewUtil

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
    return ViewUtil.inflate(parent.context, R.layout.page_loading_layout)
  }

  fun showEmpty() {
    showStateView(emptyView)
  }

  fun hideEmpty() {
    hideStateView()
  }


  open fun createEmptyView(): View {
    return ViewUtil.inflate(parent.context, R.layout.page_empty_layout)
  }

  fun showError() {
    showStateView(errorView)
  }

  fun hideError() {
    hideStateView()
  }

  open fun createErrorView(): View {
    return ViewUtil.inflate(parent.context, R.layout.page_error_layout)
  }
}