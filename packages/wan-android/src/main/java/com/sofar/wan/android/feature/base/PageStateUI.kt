package com.sofar.wan.android.feature.base

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

open class PageStateUI(private val parent: ViewGroup) {

  private val container by lazy {
    FrameLayout(parent.context).also {
      val width = ViewGroup.LayoutParams.MATCH_PARENT
      val height = ViewGroup.LayoutParams.MATCH_PARENT
      parent.addView(it, ViewGroup.LayoutParams(width, height))
    }
  }

  private var curStatusView: View? = null

  fun showStateView(view: View) {
    hideStateView()
    if (container.indexOfChild(view) < 0) {
      var lp = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT)
      lp.gravity = Gravity.CENTER
      container.addView(view, lp)
    }
    view.visibility = View.VISIBLE
    curStatusView = view
  }

  fun hideStateView() {
    curStatusView?.visibility = View.GONE
  }

}