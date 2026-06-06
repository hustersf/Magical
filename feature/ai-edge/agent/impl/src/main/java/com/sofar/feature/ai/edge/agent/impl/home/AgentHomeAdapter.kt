package com.sofar.feature.ai.edge.agent.impl.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.agent.AgentSourceType
import com.sofar.core.ai.edge.database.entity.AgentEntity
import com.sofar.feature.ai.edge.agent.impl.R

class AgentHomeAdapter(
  private val onItemClick: (AgentEntity) -> Unit,
  private val diffCallback: AgentHomeDiffCallback = AgentHomeDiffCallback()
) : ListAdapter<AgentEntity, AgentHomeViewHolder>(diffCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentHomeViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.feature_agent_home_adapter_item, parent, false)
    return AgentHomeViewHolder(itemView, onItemClick)
  }

  override fun onBindViewHolder(holder: AgentHomeViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item)
  }
}

class AgentHomeViewHolder(
  itemView: View,
  private val onItemClick: (AgentEntity) -> Unit
) : RecyclerView.ViewHolder(itemView) {

  private val avatarTv: TextView = itemView.findViewById(R.id.agent_avatar_tv)
  private val nameTv: TextView = itemView.findViewById(R.id.agent_name_tv)
  private val descTv: TextView = itemView.findViewById(R.id.agent_desc_tv)
  private val tagTv: TextView = itemView.findViewById(R.id.agent_tag_tv)

  fun bind(item: AgentEntity) {
    itemView.setOnClickListener { onItemClick(item) }
    // 设置头像（Emoji 或图片）
    avatarTv.text = item.avatar
    // 设置智能体名称
    nameTv.text = item.name
    // 设置系统提示词作为描述（如果不为空）
    descTv.text = item.systemPrompt
    tagTv.visibility = if (item.sourceType == AgentSourceType.SYSTEM) View.VISIBLE else View.GONE
  }
}