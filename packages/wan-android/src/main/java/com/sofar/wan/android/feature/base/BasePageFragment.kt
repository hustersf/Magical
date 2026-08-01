package com.sofar.wan.android.feature.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sofar.core.common.extension.isNetworkAvailable
import com.sofar.wan.android.App
import com.sofar.wan.android.paging.LoadState
import com.sofar.wan.android.paging.PageFragment
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 封装业务的通用逻辑
 */
abstract class BasePageFragment<MODEL : Any> : PageFragment<MODEL>() {

  private lateinit var pageStateUI: SimplePageStateUI

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pageStateUI = onCreatePageStateUI()
    lifecycleScope.launch {
      pageList().loadStateFlow
        .map { it.refresh }
        .distinctUntilChanged()
        .collect {
          pageStateChange(it)
        }
    }
  }

  private fun pageStateChange(loadState: LoadState) {
    val isEmpty = pageList().isEmpty()
    if (loadState is LoadState.NotLoading) {
      if (isEmpty) pageStateUI.showEmpty() else pageStateUI.hideStateView()
      return
    }

    if (loadState is LoadState.Error) {
      if (isEmpty) {
        pageStateUI.showError()
      }
      if (!App.getAppContext().isNetworkAvailable()) {
        Toast.makeText(App.getAppContext(), "网络错误", Toast.LENGTH_SHORT).show()
      }
      return
    }

    if (loadState is LoadState.Loading) {
      if (isEmpty) {
        pageStateUI.showLoading()
      }
    }
  }

  open fun onCreatePageStateUI(): SimplePageStateUI {
    return SimplePageStateUI(view as ViewGroup)
  }
}