package com.sofar.feature.ai.edge.models.logic

import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.storage.AppStorageHub
import com.sofar.core.common.state.Event

data class ModelManagerUiState(
  val loadingModelAllowlist: Boolean = true,
  val models: List<Model> = emptyList(),
  val modelDownloadStatus: Map<String, ModelDownloadStatus> = emptyMap(),
  val errorMessage: Event<String>? = null,
  val storageSnapshot: AppStorageHub.StorageSnapshot? = null,
  val isScanningStorage: Boolean = true,
)

data class ModelUiState(
  val model: Model,
  val status: ModelDownloadStatus?
)
