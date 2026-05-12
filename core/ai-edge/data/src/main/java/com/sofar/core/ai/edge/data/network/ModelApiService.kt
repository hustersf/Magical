package com.sofar.core.ai.edge.data.network

import com.sofar.core.ai.edge.data.model.ModelAllowlist
import retrofit2.http.GET

interface ModelApiService {

  @GET("model_allowlist.json")
  suspend fun modelList(): Result<ModelAllowlist>
}