package com.sofar.feature.ai.edge.models.impl

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil

class ModelDiffCallback : DiffUtil.ItemCallback<ModelUiState>() {

  override fun areItemsTheSame(
    oldItem: ModelUiState,
    newItem: ModelUiState
  ): Boolean {
    return oldItem.model.name == newItem.model.name
  }

  override fun areContentsTheSame(
    oldItem: ModelUiState,
    newItem: ModelUiState
  ): Boolean {
    return oldItem == newItem
  }

  override fun getChangePayload(oldItem: ModelUiState, newItem: ModelUiState): Any? {
    if (oldItem.model == newItem.model && oldItem.status != newItem.status) {
      return Bundle().apply { putBoolean(ModelsAdapter.PAYLOAD_PROGRESS_ONLY, true) }
    }
    return super.getChangePayload(oldItem, newItem)
  }
}