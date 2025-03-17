package com.sofar.snapu.feature.daq.product

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sofar.auto.play.Playable
import com.sofar.auto.play.RecyclerViewPlayer
import com.sofar.widget.recycler.adapter.CellViewHolder

class ProductListPlayer : RecyclerViewPlayer() {

  override fun findPlayableList(): List<Playable> {
    val list = mutableListOf<Playable>()
    var maxRatio = 0f
    var maxRatioPlayable: Playable? = null
    for (i in 0 until recyclerView.childCount) {
      val childView: View = recyclerView.getChildAt(i)
      val vh: RecyclerView.ViewHolder = recyclerView.getChildViewHolder(childView)
      if (vh is CellViewHolder<*> && vh.mCell is Playable) {
        val playable = vh.mCell as Playable
        val ratio: Float = playable.getViewShowRatio()
        if (playable.canPlay() && ratio > maxRatio) {
          maxRatio = ratio
          maxRatioPlayable = playable
        }
      }
    }
    if (maxRatioPlayable != null) {
      list.add(maxRatioPlayable)
    }
    return list
  }
}