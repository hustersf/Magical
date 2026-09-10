package com.sofar.feature.ai.edge.models.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.models.AllowedModel
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatusType
import com.sofar.core.ai.edge.data.repository.DownloadRepository
import com.sofar.core.ai.edge.data.repository.ModelRepository
import com.sofar.core.ai.edge.domain.usecase.ActiveModelHolder
import com.sofar.core.ai.edge.domain.usecase.InitModelConfigUseCase
import com.sofar.core.common.state.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelsManagerViewModel @Inject constructor(
  private val modelRepository: ModelRepository,
  private val downloadRepository: DownloadRepository,
  private val initModelConfigUseCase: InitModelConfigUseCase,
  private val activeModelHolder: ActiveModelHolder,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ModelManagerUiState())
  val uiState: StateFlow<ModelManagerUiState> = _uiState.asStateFlow()

  init {
    _uiState.update { it.copy(isScanningStorage = true) }
  }

  fun selectActiveModel(model: Model) {
    activeModelHolder.updateActiveModel(model)
  }

  fun fetchConfig() {
    refreshStorageStats()
    viewModelScope.launch {
      // 先尝试获取缓存数据（秒开）
      val cachedData = initModelConfigUseCase.getModelData()
      updateUiState(cachedData.models)

      // 持续监听后台的最新数据（预加载完成或后续更新）
      initModelConfigUseCase.dataFlow
        .onEach { latestData ->
          updateUiState(latestData.models)
        }
        .catch { e ->
          _uiState.update {
            it.copy(
              loadingModelAllowlist = false,
              errorMessage = Event("${e.localizedMessage}")
            )
          }
        }
        .launchIn(viewModelScope)
    }
  }

  fun downloadModel(model: Model) {
    viewModelScope.launch {
      downloadRepository.downloadModel(model)
        .collect { downloadStatus ->
          _uiState.update {
            val updatedStatusMap =
              it.modelDownloadStatus + (model.name to downloadStatus)
            it.copy(modelDownloadStatus = updatedStatusMap)
          }

          //  终点强制同步：当任务生命周期彻底结束（成功/失败/重试）时
          if (downloadStatus.statusType == ModelDownloadStatusType.SUCCEEDED ||
            downloadStatus.statusType == ModelDownloadStatusType.FAILED ||
            downloadStatus.statusType == ModelDownloadStatusType.NOT_DOWNLOADED
          ) {
            refreshStorageStats()
          }
        }
    }
  }

  fun deleteModel(model: Model) {
    viewModelScope.launch {
      // 删除模型文件
      modelRepository.deleteModelFile(model)
      refreshStorageStats()

      // 更新状态
      _uiState.update {
        val newStatus =
          ModelDownloadStatus(statusType = ModelDownloadStatusType.NOT_DOWNLOADED)
        val updatedStatusMap = it.modelDownloadStatus + (model.name to newStatus)
        it.copy(modelDownloadStatus = updatedStatusMap)
      }
    }
  }

  private fun refreshStorageStats() {
    viewModelScope.launch {
      _uiState.update { it.copy(isScanningStorage = true) }
      val snapshot = modelRepository.getStorageSnapshot()
      _uiState.update { it.copy(storageSnapshot = snapshot, isScanningStorage = false) }
    }
  }

  private suspend fun updateUiState(modelList: List<AllowedModel>) {
    val list = mutableListOf<Model>()
    for (item in modelList) {
      list.add(item.toModel())
    }

    val currentStatusMap = list.associate { model ->
      model.name to modelRepository.getDownloadStatus(model)
    }

    _uiState.update {
      it.copy(
        loadingModelAllowlist = false,
        models = list,
        modelDownloadStatus = currentStatusMap
      )
    }
  }
}
