package com.sofar.core.ai.edge.data.entity.models

import com.sofar.core.ai.edge.data.network.ApiConst
import kotlinx.serialization.Serializable

@Serializable
data class AllowedModel(
  val name: String,
  val modelId: String,
  val modelFile: String,
  val commitHash: String,
  val description: String,
  val sizeInBytes: Long,
  val taskTypes: List<String>,
  val disabled: Boolean? = null,
  val llmSupportImage: Boolean? = null,
  val llmSupportAudio: Boolean? = null,
  val llmSupportTinyGarden: Boolean? = null,
  val llmSupportMobileActions: Boolean? = null,
  val llmSupportThinking: Boolean? = null,
  val minDeviceMemoryInGb: Int? = null,
  val bestForTaskTypes: List<String>? = null,
  val localModelFilePathOverride: String? = null,
  val url: String? = null,
) {
  fun toModel(): Model {
    val downloadUrl =
      "${ApiConst.BASE_URL}${modelId.replace('/', '_')}/resolve/master/${modelFile}"
    val isLlmModel = true
    val llmMaxToken = 1024
    return Model(
      name = name,
      version = commitHash,
      info = description,
      url = downloadUrl,
      sizeInBytes = sizeInBytes,
      minDeviceMemoryInGb = minDeviceMemoryInGb,
      downloadFileName = modelFile,
      llmSupportImage = llmSupportImage == true,
      llmSupportAudio = llmSupportAudio == true,
      llmSupportTinyGarden = llmSupportTinyGarden == true,
      llmSupportMobileActions = llmSupportMobileActions == true,
      llmSupportThinking = llmSupportThinking == true,
      llmMaxToken = llmMaxToken,
      isLlm = isLlmModel,
    )
  }
}

@Serializable
data class ModelAllowlist(val models: List<AllowedModel>)