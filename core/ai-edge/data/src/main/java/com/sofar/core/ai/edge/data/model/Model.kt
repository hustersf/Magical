package com.sofar.core.ai.edge.data.model

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
)


