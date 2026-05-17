package com.sofar.feature.ai.edge.models.impl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.common.ByteConvertUtil
import io.noties.markwon.Markwon

class ModelsAdapter : RecyclerView.Adapter<ModelViewHolder>() {

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
  private val nameTv: TextView = itemView.findViewById(R.id.model_name_tv)
  private val sizeTv: TextView = itemView.findViewById(R.id.model_size_tv)
  private val descTv: TextView = itemView.findViewById(R.id.model_description_tv)

  private val downloadBtn: Button = itemView.findViewById(R.id.download_btn)
  private val benchmarkBtn: Button = itemView.findViewById(R.id.benchmark_btn)
  private val tryBtn: Button = itemView.findViewById(R.id.try_it_btn)
  fun bind(item: Model) {
    nameTv.text = item.name
    sizeTv.text = ByteConvertUtil.formatBytes(item.sizeInBytes)

    val markwon = Markwon.create(descTv.context)
    markwon.setMarkdown(descTv, item.info)

    benchmarkBtn.visibility= View.GONE
    tryBtn.visibility= View.GONE
    downloadBtn.visibility= View.VISIBLE
  }
}