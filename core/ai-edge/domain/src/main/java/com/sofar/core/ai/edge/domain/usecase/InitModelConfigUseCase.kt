package com.sofar.core.ai.edge.domain.usecase

import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import com.sofar.core.ai.edge.data.repository.ModelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitModelConfigUseCase @Inject constructor(
  private val repository: ModelRepository,
  private val activeModelHolder: ActiveModelHolder
) {
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _dataFlow = MutableSharedFlow<ModelAllowlist>(replay = 1)
  val dataFlow get() = _dataFlow

  /**
   * 应用启动时被调用，执行 preloadModelConfig 的逻辑
   */
  operator fun invoke() {
    applicationScope.launch {
      repository.getModelAllowlist()
        .catch { e -> e.printStackTrace() } // 捕获未知的根源异常
        .collect { allowlist ->
          // 发射进 SharedFlow 缓存起来
          _dataFlow.emit(allowlist)
          // 触发全局状态对齐，离线/在线一视同仁
          val models = allowlist.models.map { it.toModel() }
          activeModelHolder.reconcileActiveModel(models)
        }
    }
  }

  suspend fun getModelData(): ModelAllowlist {
    return _dataFlow.first()
  }
}