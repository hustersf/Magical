package com.sofar.feature.ai.edge.models.impl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.model.Model

class ModelAdapter : RecyclerView.Adapter<ModelViewHolder>() {

  private val items = mutableListOf<Model>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.models_adapter_item, parent, false)
    return ModelViewHolder(itemView)
  }

  override fun onBindViewHolder(viewHolder: ModelViewHolder, position: Int) {
    val item = items[position]
    viewHolder.bind(item)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  fun submitList(newItems: List<Model>) {
    items.clear()
    items.addAll(newItems)
    notifyDataSetChanged()
  }

}

class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val nameTv: TextView = itemView.findViewById(R.id.display_name_tv)

  fun bind(item: Model) {
    nameTv.text = item.displayName
  }
}