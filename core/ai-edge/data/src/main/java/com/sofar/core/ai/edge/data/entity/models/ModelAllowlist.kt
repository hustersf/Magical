package com.sofar.core.ai.edge.data.entity.models

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
    return Model(
      name = name,
      info = description,
      sizeInBytes = sizeInBytes,
      minDeviceMemoryInGb = minDeviceMemoryInGb,
      llmSupportImage = llmSupportImage == true,
      llmSupportAudio = llmSupportAudio == true,
      llmSupportTinyGarden = llmSupportTinyGarden == true,
      llmSupportMobileActions = llmSupportMobileActions == true,
      llmSupportThinking = llmSupportThinking == true,
    )
  }
}

@Serializable
data class ModelAllowlist(val models: List<AllowedModel>)