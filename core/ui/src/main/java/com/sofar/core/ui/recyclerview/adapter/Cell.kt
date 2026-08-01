package com.sofar.core.ui.recyclerview.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class Cell<T> {

  // 使用 lateinit 延迟初始化，保证外部赋值后，内部可以直接安全使用而不需要加问号（?）
  lateinit var mViewHolder: RecyclerView.ViewHolder

  // Kotlin 属性简写形式获取位置
  val position: Int
    get() = mViewHolder.layoutPosition

  abstract fun createView(parent: ViewGroup): View

  open fun onCreate(rootView: View) {}

  open fun onBind(data: T) {}

  open fun onUnbind() {}

  open fun onDestroy() {}

  open fun onViewAttached() {}

  open fun onViewDetached() {}
}
