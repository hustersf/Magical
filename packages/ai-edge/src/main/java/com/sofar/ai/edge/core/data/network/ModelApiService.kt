package com.sofar.ai.edge.core.data.network

import com.sofar.ai.edge.core.data.model.ModelAllowlist
import retrofit2.http.GET

interface ModelApiService {

  @GET("model_allowlist.json")
  suspend fun modelList(): Result<ModelAllowlist>
}