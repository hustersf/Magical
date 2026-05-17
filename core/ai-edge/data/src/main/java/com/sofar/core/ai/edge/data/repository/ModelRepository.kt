package com.sofar.core.ai.edge.data.repository

import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import com.sofar.core.ai.edge.data.network.ApiClientHolder
import com.sofar.core.ai.edge.data.network.ModelApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ModelRepository {
  private val modelApiService: ModelApiService = ApiClientHolder.client().create()

  fun fetchModelAllowlist(): Flow<ModelAllowlist> = flow {
    val data = modelApiService.modelList()
    emit(data)
  }.catch { e ->
    // 如果网络请求失败（比如 404、500 或断网），会走到这里
    throw e
  }.flowOn(Dispatchers.IO)
}