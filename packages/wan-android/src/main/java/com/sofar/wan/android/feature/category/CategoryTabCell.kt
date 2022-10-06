package com.sofar.wan.android.feature.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sofar.base.rx.RxBus
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Kind
import com.sofar.widget.recycler.adapter.Cell

class CategoryTabCell : Cell<Kind>() {

  private lateinit var name: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.category_tab_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    name = rootView.findViewById(R.id.name)
  }

  override fun onBind(data: Kind) {
    super.onBind(data)
    name.text = data.name
    name.isSelected = data.selected
    name.setOnClickListener {
      RxBus.get().post(CategoryEvent.TabClickEvent(data))
    }
  }
}