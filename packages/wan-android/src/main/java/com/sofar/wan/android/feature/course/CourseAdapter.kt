package com.sofar.wan.android.feature.course

import com.sofar.wan.android.model.Kind
import com.sofar.widget.recycler.adapter.Cell
import com.sofar.widget.recycler.adapter.CellAdapter

class CourseAdapter : CellAdapter<Kind>() {
  private var lastPosition = -1

  override fun onCreateCell(viewType: Int): Cell<Kind> {
    return CourseCell()
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