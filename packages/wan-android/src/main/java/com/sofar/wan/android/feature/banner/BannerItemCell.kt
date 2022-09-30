package com.sofar.wan.android.feature.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Banner
import com.sofar.widget.recycler.adapter.Cell

class BannerItemCell : Cell<Banner>() {

  private lateinit var name: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.banner_item_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    name = rootView.findViewById(R.id.name)
  }

  override fun onBind(data: Banner) {
    super.onBind(data)
    name.text = data.title
  }
}