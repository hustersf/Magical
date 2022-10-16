package com.sofar.wan.android.feature.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Banner
import com.sofar.wan.android.webview.WebViewActivity
import com.sofar.widget.recycler.adapter.Cell

class BannerItemCell : Cell<Banner>() {

  private lateinit var image: ImageView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.banner_item_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    image = rootView.findViewById(R.id.image)
  }

  override fun onBind(data: Banner) {
    super.onBind(data)
    Glide.with(image).load(data.imageUrl).into(image)
    image.setOnClickListener {
      WebViewActivity.open(image.context, data.url)
    }
  }
}