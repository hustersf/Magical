package com.sofar.core.ai.edge.data.network

import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import kotlinx.serialization.json.Json
import retrofit2.http.GET

interface ModelApiService {

  @GET("model_allowlist.json")
  suspend fun modelList(): ModelAllowlist
}