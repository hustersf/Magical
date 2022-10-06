package com.sofar.wan.android.feature.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.sofar.base.rx.RxBus
import com.sofar.wan.android.R
import com.sofar.wan.android.model.Kind
import com.sofar.widget.recycler.adapter.Cell

class CourseCell : Cell<Kind>() {

  private lateinit var cover: ImageView
  private lateinit var coverWrapper: View
  override fun createView(parent: ViewGroup): View {
    return LayoutInflater.from(parent.context).inflate(R.layout.course_cell, parent, false)
  }

  override fun onCreate(rootView: View) {
    super.onCreate(rootView)
    cover = rootView.findViewById(R.id.course_cover)
    coverWrapper = rootView.findViewById(R.id.cover_wrapper)
  }

  override fun onBind(data: Kind) {
    super.onBind(data)
    Glide.with(cover).load(data.cover).into(cover)
    cover.setOnClickListener {
      RxBus.get().post(CourseEvent.CoverClickEvent(position, data))
    }
    coverWrapper.isSelected = data.selected
  }
}