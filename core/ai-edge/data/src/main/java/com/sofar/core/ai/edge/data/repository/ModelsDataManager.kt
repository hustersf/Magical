package com.sofar.core.ai.edge.data.repository

import android.content.Context
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

object ModelsDataManager {
  private lateinit var appContext: Context
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val repository = ModelRepository()
  private val _dataFlow = MutableSharedFlow<ModelAllowlist>(replay = 1)

  // 当前选中的大模型
  private val _activeModelFlow = MutableStateFlow<Model?>(null)
  val activeModelFlow: StateFlow<Model?> = _activeModelFlow.asStateFlow()

  private const val MODEL_ALLOWLIST_FILENAME = "model_allowlist.json"

  // 对外暴露只读的 Flow
  val dataFlow get() = _dataFlow

  fun init(context: Context) {
    this.appContext = context
    preloadModelConfig()
  }

  private fun preloadModelConfig() {
    applicationScope.launch {
      try {
        // 发起网络请求
        val freshData = repository.fetchModelAllowlist().first()
        // 拿到数据后，存入本地文件（异步）
        saveToLocalFile(freshData)
        // 将最新数据发射到 SharedFlow 中，replay 缓存会更新，等待中的页面也会收到
        _dataFlow.emit(freshData)
      } catch (e: Exception) {
        e.printStackTrace()
        // 预加载失败，可以打日志，不影响页面兜底逻辑
      }
    }
  }

  fun appContext(): Context {
    return appContext
  }

  // 4. 页面获取数据的方法（完美契合你的诉求）
  suspend fun getModelData(): ModelAllowlist {
    // 诉求2：先检查本地有没有缓存
    val cachedData = loadFromLocalFile()
    if (cachedData != null) {
      return cachedData // 有缓存直接返回，秒开！
    }

    // 诉求3：缓存没有，则等待预加载的数据
    // first() 会挂起当前协程，直到预加载完成并把数据 emit 进 _dataFlow
    return _dataFlow.first()
  }

  private fun modelConfigFile(): File {
    return File(appContext.getExternalFilesDir(null), MODEL_ALLOWLIST_FILENAME)
  }

  private suspend fun loadFromLocalFile(): ModelAllowlist? =
    withContext(Dispatchers.IO) {
      return@withContext try {
        val file = modelConfigFile()
        if (file.exists()) {
          val content = file.readText()
          Json.decodeFromString<ModelAllowlist>(content)
        } else null
      } catch (e: Exception) {
        null
      }
    }

  private suspend fun saveToLocalFile(data: ModelAllowlist) =
    withContext(Dispatchers.IO) {
      try {
        val file = modelConfigFile()
        val jsonString = Json.encodeToString(data)
        file.writeText(jsonString)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }


  fun updateActiveModel(model: Model) {
    _activeModelFlow.value = model
  }

  fun getActiveModelConfig(): Model? = _activeModelFlow.value
}