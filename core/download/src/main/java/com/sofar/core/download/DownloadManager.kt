package com.sofar.core.download

import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 下载协调器
 * - 同 key 任务 single-flight（只下载一次）
 * - 新调用方复用进行中的任务并共享进度
 */
class DownloadManager(
  private val accessToken: String? = null,
) {
  private val downloadClient: DownloadClient by lazy { DownloadClient(accessToken) }

  /**
   * 获取所有并发下载任务的总网速。
   *
   * @return 网速总和（字节/秒），无下载任务时为 0
   */
  fun getGlobalDownloadSpeed(): Long {
    return tasks.values.sumOf { taskEntry ->
      // 从最新状态的进度中获取网速，避免维护冗余字段
      (taskEntry.stateFlow.replayCache.lastOrNull() as? DownloadTaskState.Downloading)?.progress?.speedBytesPerSec
        ?: 0L
    }
  }

  /**
   * 非阻塞任务入口：same-flight 防重复，返回状态流供细粒度订阅。
   *
   * 同一 key 的任务只发起一次下载，所有调用方实时订阅 Waiting/Downloading/Succeeded/Failed 状态。
   * 需要手动处理所有状态变化（包括开始、进度、完成）。
   *
   * 场景差异对比：
   * - 简单场景（等完成就用文件）→ 用 await()
   * - 复杂场景（需要实时响应所有状态）→ 用 enqueueOrJoin()
   *   如：下载管理 UI 需要显示 "检查中/下载中/完成" 三个阶段
   *
   * @param request 下载请求
   * @return 状态流（包含所有四种状态）
   */
  fun enqueueOrJoin(request: DownloadRequest): Flow<DownloadTaskState> {
    val taskKey = request.taskKey()
    // computeIfAbsent：如果 key 不存在，调用 lambda 创建新值并存入
    val taskEntry = tasks.computeIfAbsent(taskKey) {
      createTaskEntry(taskKey = taskKey, request = request)
    }
    return taskEntry.stateFlow.asSharedFlow()
  }

  /**
   * 挂起等待下载完成：简化入口，自动复用进行中任务，防重复下载。
   *
   * 如果同 key 任务已在运行，直接加入并共享进度，不重新发起下载。页面销毁不影响后台任务，
   * 回页后继续监听同一任务。
   *
   * 使用场景：
   * - 语音/AI 模型下载与页面生命周期隔离复用
   * - 多模块并发请求同一资源只下载一次
   * - 前台/后台切换时智能续接
   *
   * @param request 下载请求
   * @param onProgress 进度回调（仅在下载中触发，Downloading 状态）
   * @return 完成后返回目标文件
   * @throws IOException 下载失败
   *
   * @see enqueueOrJoin 如需手动处理所有状态变化
   */
  suspend fun await(
    request: DownloadRequest,
    onProgress: (DownloadProgress) -> Unit = {},
  ): File {
    val terminalState = enqueueOrJoin(request).onEach { state ->
      if (state is DownloadTaskState.Downloading) {
        onProgress(state.progress)
      }
    }.first { state ->
      state is DownloadTaskState.Succeeded || state is DownloadTaskState.Failed
    }

    return when (terminalState) {
      is DownloadTaskState.Succeeded -> terminalState.targetFile
      is DownloadTaskState.Failed -> {
        val cause = terminalState.throwable
        throw (cause as? IOException) ?: IOException(cause.message ?: "download failed", cause)
      }

      else -> throw IllegalStateException("unexpected terminal state: $terminalState")
    }
  }

  private fun createTaskEntry(taskKey: String, request: DownloadRequest): TaskEntry {
    val stateFlow = MutableSharedFlow<DownloadTaskState>(replay = 1, extraBufferCapacity = 16)
    val taskEntry = TaskEntry(stateFlow = stateFlow)
    stateFlow.tryEmit(DownloadTaskState.Waiting)

    taskScope.launch {
      try {
        downloadClient.download(
          fileUrl = request.fileUrl,
          targetFile = request.targetFile,
          totalBytes = request.totalBytes,
          tmpFile = request.tmpFile,
        ) { downloaded, resolvedTotalBytes, speedBytesPerSec, remainingMs ->
          stateFlow.tryEmit(
            DownloadTaskState.Downloading(
              progress = DownloadProgress(
                downloadedBytes = downloaded,
                totalBytes = resolvedTotalBytes,
                speedBytesPerSec = speedBytesPerSec,
                remainingMs = remainingMs,
              ),
            ),
          )
        }

        stateFlow.tryEmit(DownloadTaskState.Succeeded(request.targetFile))
      } catch (throwable: Throwable) {
        stateFlow.tryEmit(DownloadTaskState.Failed(throwable))
      } finally {
        tasks.remove(taskKey)
      }
    }

    return taskEntry
  }

  data class DownloadRequest(
    val fileUrl: String,
    val targetFile: File,
    val totalBytes: Long = UNKNOWN_TOTAL_BYTES,
    val tmpFile: File? = null,
  ) {
    fun taskKey(): String {
      val tmpPath = tmpFile?.absolutePath.orEmpty()
      return "$fileUrl|${targetFile.absolutePath}|$tmpPath"
    }
  }

  data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val remainingMs: Long,
  )

  sealed class DownloadTaskState {
    data object Waiting : DownloadTaskState()

    // 包含进度信息，避免与 DownloadProgress 重复定义
    data class Downloading(val progress: DownloadProgress) : DownloadTaskState()

    data class Succeeded(val targetFile: File) : DownloadTaskState()
    data class Failed(val throwable: Throwable) : DownloadTaskState()
  }

  private data class TaskEntry(
    val stateFlow: MutableSharedFlow<DownloadTaskState>,
  )

  private companion object {
    private const val UNKNOWN_TOTAL_BYTES = -1L
    private val tasks = ConcurrentHashMap<String, TaskEntry>()
    private val taskScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  }
}