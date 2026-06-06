package com.sofar.feature.ai.edge.agent.impl.home

import androidx.recyclerview.widget.DiffUtil
import com.sofar.core.ai.edge.database.entity.AgentEntity

class AgentHomeDiffCallback : DiffUtil.ItemCallback<AgentEntity>() {

  override fun areItemsTheSame(
    oldItem: AgentEntity,
    newItem: AgentEntity
  ): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(
    oldItem: AgentEntity,
    newItem: AgentEntity
  ): Boolean {
    return oldItem == newItem
  }
}