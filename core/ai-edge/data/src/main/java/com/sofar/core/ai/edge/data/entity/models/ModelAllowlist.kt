package com.sofar.core.ai.edge.data.entity.models

import com.sofar.core.ai.edge.data.entity.llm.Accelerator
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_ACCELERATORS
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_MAX_TOKEN
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TEMPERATURE
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TOPK
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_TOPP
import com.sofar.core.ai.edge.data.entity.llm.DEFAULT_VISION_ACCELERATOR
import com.sofar.core.ai.edge.data.network.ApiConst
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DefaultConfig(
  @SerialName("topK") val topK: Int?,
  @SerialName("topP") val topP: Float?,
  @SerialName("temperature") val temperature: Float?,
  @SerialName("accelerators") val accelerators: String?,
  @SerialName("visionAccelerator") val visionAccelerator: String?,
  @SerialName("maxContextLength") val maxContextLength: Int?,
  @SerialName("maxTokens") val maxTokens: Int?,
)

@Serializable
data class AllowedModel(
  val name: String,
  val modelId: String,
  val modelFile: String,
  val commitHash: String,
  val description: String,
  val sizeInBytes: Long,
  val defaultConfig: DefaultConfig,
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
    val llmMaxToken = defaultConfig.maxTokens ?: DEFAULT_MAX_TOKEN
    val defaultTopK: Int = defaultConfig.topK ?: DEFAULT_TOPK
    val defaultTopP: Float = defaultConfig.topP ?: DEFAULT_TOPP
    val defaultTemperature: Float = defaultConfig.temperature ?: DEFAULT_TEMPERATURE
    var accelerators: List<Accelerator> = DEFAULT_ACCELERATORS
    var visionAccelerator: Accelerator = DEFAULT_VISION_ACCELERATOR
    if (defaultConfig.accelerators != null) {
      val items = defaultConfig.accelerators.split(",")
      accelerators = mutableListOf()
      for (item in items) {
        if (item == "cpu") {
          accelerators.add(Accelerator.CPU)
        } else if (item == "gpu") {
          accelerators.add(Accelerator.GPU)
        } else if (item == "npu") {
          accelerators.add(Accelerator.NPU)
        }
      }
    }
    if (defaultConfig.visionAccelerator != null) {
      val accelerator = defaultConfig.visionAccelerator
      if (accelerator == "cpu") {
        visionAccelerator = Accelerator.CPU
      } else if (accelerator == "gpu") {
        visionAccelerator = Accelerator.GPU
      } else if (accelerator == "npu") {
        visionAccelerator = Accelerator.NPU
      }
    }
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
      isLlm = isLlmModel,
      llmMaxToken = llmMaxToken,
      llmTopK = defaultTopK,
      llmTopP = defaultTopP,
      llmTemperature = defaultTemperature,
      accelerators = accelerators,
      visionAccelerator = visionAccelerator
    )
  }
}

@Serializable
data class ModelAllowlist(val models: List<AllowedModel>)