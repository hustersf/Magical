package com.sofar.core.ai.edge.data.entity.models

import com.sofar.core.ai.edge.data.entity.llm.Accelerator

private val NORMALIZE_NAME_REGEX = Regex("[^a-zA-Z0-9]")

data class Model(
  val name: String,
  val displayName: String = "",
  val info: String = "",
  val learnMoreUrl: String = "",
  val bestForTaskIds: List<String> = listOf(),
  val minDeviceMemoryInGb: Int? = null,
  val url: String = "",
  val sizeInBytes: Long = 0L,
  val downloadFileName: String = "_",
  val version: String = "_",
  val isLlm: Boolean = false,
  /** Whether to show the "run again" button in the UI. */
  val showRunAgainButton: Boolean = true,

  /** Whether to show the "benchmark" button in the UI. */
  val showBenchmarkButton: Boolean = true,

  /** Indicates whether the model is a zip file. */
  val isZip: Boolean = false,

  /** The name of the directory to unzip the model to (if it's a zip file). */
  val unzipDir: String = "",

  /** Whether the LLM model supports image input. */
  val llmSupportImage: Boolean = false,

  /** Whether the LLM model supports audio input. */
  val llmSupportAudio: Boolean = false,

  /** Whether the LLM model supports tiny garden. */
  val llmSupportTinyGarden: Boolean = false,

  /** Whether the LLM model supports mobile actions. */
  val llmSupportMobileActions: Boolean = false,

  /** Whether the LLM model supports thinking mode. */
  val llmSupportThinking: Boolean = false,

  /** The max token for llm model. */
  val llmMaxToken: Int = 1024,

  val llmTopK: Int = 64,
  val llmTopP: Float = 0.95f,
  val llmTemperature: Float = 1.0f,

  /** Compatible accelerators. */
  val accelerators: List<Accelerator> = listOf(),

  /** Accelerator for running vision encoder. */
  val visionAccelerator: Accelerator = Accelerator.GPU,

  var normalizedName: String = "",
) {

  init {
    normalizedName = NORMALIZE_NAME_REGEX.replace(name, "_")
  }
}

enum class ModelDownloadStatusType {
  NOT_DOWNLOADED,
  PARTIALLY_DOWNLOADED,
  IN_PROGRESS,
  UNZIPPING,
  SUCCEEDED,
  FAILED,
}

data class ModelDownloadStatus(
  val statusType: ModelDownloadStatusType,
  val totalBytes: Long = 0,
  val receivedBytes: Long = 0,
  val errorMessage: String = "",
  val bytesPerSecond: Long = 0,
  val remainingMs: Long = 0,
)