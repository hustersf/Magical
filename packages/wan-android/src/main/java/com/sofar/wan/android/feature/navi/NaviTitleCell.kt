package com.sofar.wan.android.feature.navi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Tag
import com.sofar.widget.recycler.adapter.Cell

class NaviTitleCell : Cell<Tag>() {
  private lateinit var name: TextView

  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.navi_title_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    name = rootView.findViewById(R.id.name)
  }

  override fun onBind(data: Tag) {
    super.onBind(data)
    name.text = data.name
  }
}