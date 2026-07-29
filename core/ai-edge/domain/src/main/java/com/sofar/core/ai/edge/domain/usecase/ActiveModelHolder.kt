package com.sofar.core.ai.edge.domain.usecase

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.sofar.core.ai.edge.data.entity.models.Model
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveModelHolder @Inject constructor(
  @param:ApplicationContext private val context: Context
) {

  companion object {
    private const val TAG = "ActiveModelHolder"
    private const val MODEL_PREFS_NAME = "ai_edge_active_model_prefs"
    private const val KEY_LAST_ACTIVE_MODEL_NAME = "last_active_model_name"
  }

  private val _activeModelFlow = MutableStateFlow<Model?>(null)
  val activeModelFlow: StateFlow<Model?> = _activeModelFlow.asStateFlow()

  private val sharedPrefs = context.getSharedPreferences(MODEL_PREFS_NAME, Context.MODE_PRIVATE)

  fun updateActiveModel(model: Model) {
    _activeModelFlow.value = model
    sharedPrefs.edit { putString(KEY_LAST_ACTIVE_MODEL_NAME, model.name) }
    Log.d(TAG, "手动切换/首次激活模型并持久化: ${model.name}")
  }

  fun reconcileActiveModel(availableModels: List<Model>) {
    if (availableModels.isEmpty()) return

    val currentModel = _activeModelFlow.value
    if (currentModel != null && availableModels.any { it.name == currentModel.name }) {
      return
    }

    val lastSavedModelName = sharedPrefs.getString(KEY_LAST_ACTIVE_MODEL_NAME, null)
    val savedModel = availableModels.firstOrNull { it.name == lastSavedModelName }

    if (savedModel != null) {
      _activeModelFlow.value = savedModel
      Log.d(TAG, "自动恢复历史选中的模型: ${savedModel.name}")
    } else {
      val defaultModel = availableModels.first()
      Log.d(TAG, "首次使用：激活第一个模型: ${defaultModel.name}")
      updateActiveModel(defaultModel)
    }
  }

  fun getActiveModelConfig(): Model? = _activeModelFlow.value
}