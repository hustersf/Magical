package com.sofar.core.speech.internal.model

import android.content.Context
import com.sofar.core.download.DownloadManager
import com.sofar.core.download.ZipUtil
import com.sofar.core.speech.internal.contract.SpeechRecognitionEngineModelEvent
import com.sofar.core.speech.sherpa.SherpaOnnxModelConfig
import com.sofar.core.speech.sherpa.SherpaOnnxModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

internal interface SpeechModelProvider {
  val model: SpeechModel

  /**
   * 确保模型可用（线程安全，内部 Mutex 防止并发下载）。
   *
   * @param onEvent 生命周期事件回调，从 IO 线程或下载回调线程调用,
   *                使用 [kotlinx.coroutines.channels.ProducerScope.trySend] 等线程安全方法接收。
   */
  suspend fun ensureReady(
    onEvent: (SpeechRecognitionEngineModelEvent) -> Unit = {},
  ): SpeechModel

  fun isModelReady(): Boolean
}

/**
 * Built-in SenseVoice model provider.
 *
 * The public speech API stays transparent: callers only start/stop recognition. This provider makes
 * sure the fixed remote model archive is available locally before the sherpa engine records audio.
 */
internal class DefaultSpeechModelProvider(
  context: Context,
  private val downloadManager: DownloadManager = DownloadManager(),
) : SpeechModelProvider {
  private val appContext = context.applicationContext
  private val modelBaseDir = File(appContext.getExternalFilesDir(null), MODEL_BASE_DIR)
  private val modelDir = File(modelBaseDir, MODEL_DIR_NAME)
  private val archiveFile = File(modelBaseDir, MODEL_ARCHIVE_FILE_NAME)
  private val tmpArchiveFile = File(modelBaseDir, "$MODEL_ARCHIVE_FILE_NAME.tmp")
  private val readyMarkerFile = File(modelBaseDir, READY_MARKER_FILE_NAME)
  private val mutex = Mutex()

  override val model: SpeechModel = SpeechModel(
    id = MODEL_ID,
    name = MODEL_NAME,
    languageCode = MODEL_LANGUAGE_CODE,
    localDir = modelDir.absolutePath,
    sherpaConfig = SherpaOnnxModelConfig(
      modelType = SherpaOnnxModelType.SenseVoice,
      tokens = TOKENS_FILE_NAME,
      model = MODEL_FILE_NAME,
    ),
  )

  // 确保模型可用（幂等 + 线程安全）：
  // Checking → [Downloading → Unzipping] → Ready
  // Mutex 防止并发重复下载，已就绪则快速返回
  override suspend fun ensureReady(
    onEvent: (SpeechRecognitionEngineModelEvent) -> Unit,
  ): SpeechModel {
    // 快速路径：模型文件已存在，无需持锁
    onEvent(SpeechRecognitionEngineModelEvent.Checking)
    if (isModelReady()) {
      onEvent(SpeechRecognitionEngineModelEvent.Ready)
      return model
    }

    // 慢速路径：需要下载/解压，用 Mutex 防止并发重复操作
    mutex.withLock {
      withContext(Dispatchers.IO) {
        // 可能等锁期间已被另一个协程完成了下载
        if (isModelReady()) return@withContext

        modelBaseDir.mkdirs()
        if (!isArchiveReady()) {
          tmpArchiveFile.parentFile?.mkdirs()
          downloadManager.await(
            request = DownloadManager.DownloadRequest(
              fileUrl = MODEL_ARCHIVE_URL,
              targetFile = archiveFile,
              tmpFile = tmpArchiveFile,
            ),
          ) { progress ->
            val normalizedProgress = if (progress.totalBytes > 0) {
              ((progress.downloadedBytes.toFloat() / progress.totalBytes) * 100)
                .toInt()
                .coerceIn(0, 100)
            } else {
              0
            }
            onEvent(SpeechRecognitionEngineModelEvent.Downloading(normalizedProgress))
          }
        }

        if (!isModelReady()) {
          onEvent(SpeechRecognitionEngineModelEvent.Unzipping)
          modelDir.deleteRecursively()
          readyMarkerFile.delete()
          modelDir.mkdirs()
          runCatching {
            ZipUtil.unzip(zipFile = archiveFile, deleteSource = true)
          }.onFailure {
            archiveFile.delete()
            tmpArchiveFile.delete()
            throw it
          }
        }

        check(isModelReady()) {
          "Downloaded speech model is incomplete. Expected files: ${requiredFiles().joinToString()}"
        }
        readyMarkerFile.writeText(MODEL_ARCHIVE_URL)
      }
    }

    onEvent(SpeechRecognitionEngineModelEvent.Ready)
    return model
  }

  private fun isArchiveReady(): Boolean {
    return archiveFile.isFile && archiveFile.length() > 0L
  }

  override fun isModelReady(): Boolean {
    return requiredFiles().all { File(model.localDir, it).isFile }
  }

  private fun requiredFiles(): List<String> {
    return model.sherpaConfig.requiredFiles() + VAD_MODEL_PATH
  }

  internal companion object {
    const val MODEL_ID = "default-sense-voice"
    const val MODEL_NAME = "SenseVoice zh/en/ja/ko/yue int8"
    const val MODEL_LANGUAGE_CODE = "zh-CN"
    const val VAD_MODEL_PATH = "silero_vad.onnx"
    private const val MODEL_FILE_NAME = "model.int8.onnx"
    private const val TOKENS_FILE_NAME = "tokens.txt"

    /**
     * Replace this with your production archive URL.
     *
     * The zip should contain these files at root level:
     * - model.int8.onnx
     * - tokens.txt
     * - silero_vad.onnx
     *
     * The provider extracts that zip into MODEL_DIR_NAME (zip filename without .zip).
     */
    const val MODEL_ARCHIVE_URL =
      "https://modelscope.cn/models/sofarsogood001/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8/resolve/master/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-int8-2024-07-17.zip"

    private val MODEL_ARCHIVE_FILE_NAME =
      MODEL_ARCHIVE_URL.substringAfterLast('/').substringBefore('?').substringBefore('#')
    private val MODEL_DIR_NAME = MODEL_ARCHIVE_FILE_NAME.removeSuffix(".zip")

    private const val MODEL_BASE_DIR = "speech/models"
    private const val READY_MARKER_FILE_NAME = ".${MODEL_ID}.ready"
  }
}