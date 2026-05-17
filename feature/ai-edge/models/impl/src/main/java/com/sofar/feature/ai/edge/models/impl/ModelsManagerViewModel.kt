package com.sofar.feature.ai.edge.models.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import com.sofar.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModelsManagerViewModel : ViewModel() {

  private val _uiState = MutableStateFlow<UiState<ModelAllowlist>>(UiState.Loading)
  val uiState: StateFlow<UiState<ModelAllowlist>> = _uiState.asStateFlow()

  fun fetchConfig() {
    viewModelScope.launch {
      // 先尝试获取缓存数据（秒开）
      val cachedData = ModelsDataManager.getModelData()
      if (cachedData != null) {
        _uiState.value = UiState.Success(cachedData)
      }

      // 持续监听后台的最新数据（预加载完成或后续更新）
      ModelsDataManager.dataFlow
        .onEach { latestData ->
          // 无论何时拿到最新数据，都更新为 Success 状态
          _uiState.value = UiState.Success(latestData)
        }
        .catch { e ->
          // 捕获 Flow 链路中可能发生的异常，更新为 Error 状态
          _uiState.value = UiState.Error(e)
        }
        .launchIn(viewModelScope) // 在 viewModelScope 中启动收集
    }
  }
}