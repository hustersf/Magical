package com.sofar.wan.android.feature.navi

import android.text.TextUtils
import com.sofar.wan.android.model.Tag
import com.sofar.widget.recycler.adapter.Cell
import com.sofar.widget.recycler.adapter.CellAdapter

class NaviTabAdapter : CellAdapter<Tag>() {

  private var lastPosition = -1

  override fun onCreateCell(viewType: Int): Cell<Tag> {
    return NaviTabCell()
  }

  fun selectTag(name: String): Int {
    var pos = -1
    items.forEachIndexed { index, it ->
      if (TextUtils.equals(it.name, name)) {
        pos = index
      }
    }
    if (pos != -1) {
      selectPosition(pos)
    }
    return pos
  }

  fun selectPosition(position: Int) {
    if (position == lastPosition) {
      return
    }

    var lastItem = getItem(lastPosition)
    lastItem?.selected = false
    notifyItemChanged(lastPosition)

    lastPosition = position
    var curItem = getItem(position)
    curItem?.selected = true
    notifyItemChanged(position)
  }

}