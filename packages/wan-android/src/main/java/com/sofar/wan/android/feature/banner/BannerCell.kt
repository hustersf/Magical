package com.sofar.wan.android.feature.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.sofar.base.widget.banner.BannerAutoHelper
import com.sofar.base.widget.banner.BannerIndicator
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Banners
import com.sofar.wan.android.utility.CommonUtil
import com.sofar.widget.recycler.adapter.Cell

class BannerCell : Cell<Banners>() {

  private lateinit var viewPager2: ViewPager2
  private lateinit var indicator: BannerIndicator
  private lateinit var bannerAutoHelper: BannerAutoHelper
  private lateinit var adapter: BannerAdapter

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.banner_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    viewPager2 = rootView.findViewById(R.id.banner_viewpager)
    indicator = rootView.findViewById(R.id.banner_indicator)
    bannerAutoHelper = BannerAutoHelper(viewPager2, indicator)
    adapter = BannerAdapter()
    viewPager2.adapter = adapter
  }

  override fun onBind(data: Banners) {
    super.onBind(data)
    adapter.items = data.banners
    adapter.notifyDataSetChanged()
    var size = CommonUtil.dip2px(5f)
    var margin = CommonUtil.dip2px(2f)
    indicator.initIndicatorItems(data.banners.size, size, size, margin, margin)
    bannerAutoHelper.setCount(data.banners.size)
    bannerAutoHelper.start()
  }

  override fun onDestroy() {
    super.onDestroy()
    bannerAutoHelper.destroy()
  }

}