package com.sofar.core.ai.edge.data.repository

import android.content.Context
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.deleteModelFile
import com.sofar.core.ai.edge.data.entity.models.getDownloadStatus
import com.sofar.core.ai.edge.data.network.ApiClientHolder
import com.sofar.core.ai.edge.data.network.ModelApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class ModelRepository(context: Context) {
  // 安全提取 ApplicationContext，防止潜在的内存泄漏
  private val appContext = context.applicationContext
  private val modelApiService: ModelApiService = ApiClientHolder.modelApiService

  companion object {
    private const val MODEL_ALLOWLIST_FILENAME = "model_allowlist.json"
  }

  fun getDownloadStatus(model: Model): ModelDownloadStatus {
    return model.getDownloadStatus(appContext)
  }

  suspend fun deleteModelFile(model: Model) {
    model.deleteModelFile(appContext)
  }

  fun getModelAllowlist(): Flow<ModelAllowlist> = flow {
    // 优先读取并向外发射本地文件缓存（秒开体验）
    val localData = loadFromLocalFile()
    if (localData != null) {
      emit(localData)
    }

    // 紧接着发起网络请求同步最新数据
    try {
      val freshData = modelApiService.modelList()
      // 网络请求成功，持久化到本地文件覆盖
      saveToLocalFile(freshData)
      // 向外发射最新的网络数据，通知下游
      emit(freshData)
    } catch (e: Exception) {
      e.printStackTrace()
      // 优雅降级：如果本地已经发射过缓存，网络报错则不抛出异常，保证全应用离线可用
      if (localData == null) {
        throw e
      }
    }
  }.flowOn(Dispatchers.IO)

  fun fetchModelAllowlist(): Flow<ModelAllowlist> = flow {
    val data = modelApiService.modelList()
    emit(data)
  }.catch { e ->
    throw e
  }.flowOn(Dispatchers.IO)

  private fun modelConfigFile(): File {
    return File(appContext.getExternalFilesDir(null), MODEL_ALLOWLIST_FILENAME)
  }

  suspend fun loadFromLocalFile(): ModelAllowlist? =
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

  suspend fun saveToLocalFile(data: ModelAllowlist) =
    withContext(Dispatchers.IO) {
      try {
        val file = modelConfigFile()
        val jsonString = Json.encodeToString(data)
        file.writeText(jsonString)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
}