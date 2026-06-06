package com.sofar.core.ai.edge.data.entity.models

import android.content.Context
import com.sofar.core.ai.edge.data.entity.llm.Accelerator
import com.sofar.core.ai.edge.data.storage.AppStorageHub
import java.io.File

private val NORMALIZE_NAME_REGEX = Regex("[^a-zA-Z0-9]")
const val TMP_FILE_EXT = "tmp"

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

  fun getBaseDir(context: Context): String {
    return listOf(
      AppStorageHub.modelsDir(context).absolutePath,
      normalizedName,
      version
    ).joinToString(File.separator)
  }

  fun getPath(context: Context, fileName: String = downloadFileName): String {
    val baseDir = getBaseDir(context)
    return if (isZip && unzipDir.isNotEmpty()) {
      listOf(baseDir, unzipDir).joinToString(File.separator)
    } else {
      listOf(baseDir, fileName).joinToString(File.separator)
    }
  }

  fun getTmpPath(context: Context): String {
    return getPath(context, fileName = "$downloadFileName.$TMP_FILE_EXT")
  }

  fun getDownloadStatus(context: Context): ModelDownloadStatus {
    val fileOrDir = File(getPath(context))
    val tmpFile = File(getTmpPath(context))

    return when {
      // 状态 A：正式文件完美存在、且是个单文件、体积大于 0
      fileOrDir.exists() && fileOrDir.isFile && fileOrDir.length() == sizeInBytes -> {
        ModelDownloadStatus(statusType = ModelDownloadStatusType.SUCCEEDED)
      }

      // 状态 B：正式文件还没就绪，但发现本地躺着一个有体积的 .tmp 临时文件！
      tmpFile.exists() && tmpFile.isFile && tmpFile.length() > 0 -> {
        ModelDownloadStatus(
          statusType = ModelDownloadStatusType.PARTIALLY_DOWNLOADED,
          totalBytes = this.sizeInBytes,   // 线上预计的总大小
          receivedBytes = tmpFile.length()  // 本地上一次已经下载完的断点字节数
        )
      }

      // 状态 C：全都没有，白纸一张
      else -> {
        ModelDownloadStatus(statusType = ModelDownloadStatusType.NOT_DOWNLOADED)
      }
    }
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