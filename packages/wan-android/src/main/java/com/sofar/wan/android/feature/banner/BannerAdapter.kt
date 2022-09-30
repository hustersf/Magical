package com.sofar.wan.android.feature.banner

import com.sofar.wan.android.model.Banner
import com.sofar.widget.recycler.adapter.Cell
import com.sofar.widget.recycler.adapter.CellAdapter

class BannerAdapter : CellAdapter<Banner>() {

  var loopCount = 10000

  override fun onCreateCell(viewType: Int): Cell<Banner> {
    return BannerItemCell()
  }


  override fun getItemCount(): Int {
    return if (super.getItemCount() > 1) super.getItemCount() * loopCount else super.getItemCount()
  }

  override fun getItem(position: Int): Banner? {
    return super.getItem(position % super.getItemCount())
  }
}