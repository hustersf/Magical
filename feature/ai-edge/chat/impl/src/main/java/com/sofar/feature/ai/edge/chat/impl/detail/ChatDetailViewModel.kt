package com.sofar.feature.ai.edge.chat.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.repository.AgentRepository
import com.sofar.core.ai.edge.data.repository.ChatRepository
import com.sofar.core.ai.edge.domain.usecase.ActiveModelHolder
import com.sofar.feature.ai.edge.chat.impl.detail.image.SelectedImageState
import com.sofar.feature.ai.edge.chat.impl.detail.voice.VoiceInputUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
  private val repository: ChatRepository,
  private val agentRepository: AgentRepository,
  private val activeModelHolder: ActiveModelHolder,
) : ViewModel() {

  //  负责持续性状态
  private val _uiState = MutableStateFlow(ChatDetailUiState())
  val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

  // 负责一次性事件（使用 Channel，确保事件不丢、不重、阅后即焚）
  private val _effectChannel = Channel<ChatDetailEffect>(Channel.BUFFERED)
  val effectFlow: Flow<ChatDetailEffect> = _effectChannel.receiveAsFlow()

  // 观察离线数据库变动的工作句柄，换对话时用来强行掐断老的流
  private var dbObservationJob: Job? = null

  private var modelObservationJob: Job? = null

  private val _currentActiveModel = MutableStateFlow<Model?>(null)

  /**
   * 🧱 业务一：单向挂载观察本地 Room 消息数据流
   * 🎯 大闭环关键：只要大模型在推理结束时往数据库塞数据，这根管道会瞬间捕捉，
   * 自动把最新列表同步进 _uiState.messages，从而触发 Activity 里的 ListAdapter 刷新！
   */
  fun init(sessionId: String, agentId: String? = null) {
    // 如果之前已经挂载了别的对话，先安全断开，防止历史数据串门
    dbObservationJob?.cancel()

    // 强力挂载 Room 流监听
    dbObservationJob = viewModelScope.launch {
      val session = repository.getSessionById(sessionId)
      val agent = agentId?.let { agentRepository.getAgentById(it) }
      val title = if (agentId != null) {
        // 去 agent 表里捞对应的智能体名称（如“英语口语教练”）
        agentRepository.getAgentById(agentId)?.name ?: session?.title ?: ""
      } else {
        session?.title ?: ""
      }
      _uiState.update {
        it.copy(sessionTitle = title)
      }

      launch {
        activeModelHolder.activeModelFlow.collect { freshModel ->
          _currentActiveModel.value = freshModel
          if (freshModel == null) {
            alertEffect("当前未在模型Tab激活任何本地大模型")
          } else {
            // 初始化
            prepareEngine(
              freshModel,
              sessionId,
              agent?.systemPrompt
            )
          }
        }
      }


      repository.getMessagesBySession(sessionId)
        .collect { freshMessages ->
          _uiState.update { it.copy(messages = freshMessages) }
        }
    }
  }

  /**
   * 🧱 业务二：初始化并唤醒端侧 AI 引擎
   * 对应 Activity 的 initData 阶段，或者用户去 Tab 5 页面动态重载大模型时调用
   */
  private fun prepareEngine(
    model: Model,
    sessionId: String,
    agentSystemPrompt: String?
  ) {
    // 开启界面思考锁，置灰发送键，控制 UI 展示转圈 Loading 状态
    _uiState.update { it.copy(isEngineLoading = true) }

    // 🚀 在主线程作用域中无忧启动协程，保持 UI 的极速响应
    viewModelScope.launch {
      val errorResult = repository.initializeModel(
        model = model,
        sessionId = sessionId,
        agentSystemPrompt = agentSystemPrompt
      )

      // 👍 子线程的 C++ 图编译彻底闭环，协程在主线程苏醒，直接在线性代码下方丝滑刷新 UI 状态
      val isSuccess = errorResult.isEmpty()
      _uiState.update {
        it.copy(
          isEngineLoading = false,
          isModelReady = isSuccess,
        )
      }
      if (!isSuccess) {
        toastEffect(errorResult)
      }
    }
  }

  /**
   * 🧱 业务三：全能多模态消息发送（核心调度大脑）
   */
  fun performSendMessage(
    sessionId: String,
    agentId: String? = null,
    inputTextFieldValue: String,
    sandboxedImagesPath: List<String> = listOf(),
    sandboxedAudioPath: List<String> = listOf()
  ) {
    val userPrompt = inputTextFieldValue.trim()
    if (userPrompt.isEmpty() && sandboxedImagesPath.isEmpty() && sandboxedAudioPath.isEmpty()) return

    val activeModel = _currentActiveModel.value ?: run {
      alertEffect("当前未在设置中激活任何本地大模型")
      return
    }

    if (_uiState.value.isAiResponding) return

    // 模型思考中
    _uiState.update {
      it.copy(
        isAiResponding = true,
        currentStreamingText = null,
        selectedImages = emptyList(),
      )
    }

    viewModelScope.launch {
      // 🌊 核心流式对接：调度底层的 LiteRT 推理管道
      repository.sendMessageStream(
        sessionId = sessionId, model = activeModel, input = userPrompt,
        inputImagesPath = sandboxedImagesPath, inputAudioPath = sandboxedAudioPath
      )
        .catch { exception ->
          // 遇错截流，将专属的响应状态拉回 false
          _uiState.update {
            it.copy(
              isAiResponding = false,
              currentStreamingText = null,
            )
          }
          toastEffect(exception.message)
        }
        .onCompletion {
          // 整个大模型流式喷射谢幕，重置响应指标，把画面权完美交还给 Room
          repository.updateSessionAgentId(sessionId, agentId)
          val session = repository.getSessionById(sessionId)
          _uiState.update {
            it.copy(
              sessionTitle = session?.title ?: "",
              isAiResponding = false,
              currentStreamingText = null
            )
          }
        }
        .collect { accumulatedText ->
          // 🚀【修复核心：打字机流转】：高频把全量拼好的词通过单状态送往 Activity 看板
          // 💡 注意：在此期间，isAiResponding 依旧保持为 true 锁存，以驱动前台正方形打断按钮的常驻
          _uiState.update {
            it.copy(
              currentStreamingText = accumulatedText
            )
          }
        }
    }
  }

  /**
   * 🧱 业务四：一键掐断大模型输出
   */
  fun stopModelResponse() {
    _currentActiveModel.value?.let { repository.stopModelResponse(it) }
    _uiState.update {
      it.copy(
        isAiResponding = false,
        currentStreamingText = null
      )
    }
  }

  private fun toastEffect(msg: String?) {
    msg?.let {
      _effectChannel.trySend(ChatDetailEffect.ShowToast(it))
    }
  }

  private fun alertEffect(msg: String?) {
    msg?.let {
      _effectChannel.trySend(ChatDetailEffect.ShowAlert(it))
    }
  }

  fun updateSelectedImages(newImages: List<SelectedImageState>) {
    _uiState.update { currentState ->
      currentState.copy(selectedImages = newImages)
    }
  }

  fun onInputTextChanged(text: String) {
    _uiState.update { it.copy(chatInputText = text) }
  }

  private fun updateVoiceState(reducer: VoiceInputUiState.() -> VoiceInputUiState) {
    _uiState.update { it.copy(voiceState = it.voiceState.reducer()) }
  }

  fun toggleInputMode() {
    updateVoiceState { copy(isVoiceMode = !isVoiceMode) }
  }

  fun showVoiceInputUi() {
    updateVoiceState {
      copy(
        isVoiceOverlayVisible = true,
        isVoiceCanceling = false,
        voiceRecognizedText = "",
        voiceRmsDB = 0f
      )
    }
  }

  fun updateVoiceRecognizedText(text: String) {
    updateVoiceState { copy(voiceRecognizedText = text) }
  }

  fun updateVoiceRms(rmsDb: Float) {
    updateVoiceState { copy(voiceRmsDB = rmsDb) }
  }

  fun updateVoiceCanceling(canceling: Boolean) {
    updateVoiceState { copy(isVoiceCanceling = canceling) }
  }

  fun finishVoiceInputUi() {
    updateVoiceState {
      copy(
        isVoiceOverlayVisible = false,
        isVoiceCanceling = false,
        voiceRecognizedText = "",
        voiceRmsDB = 0f
      )
    }
  }

  // ---- 语音识别引擎（SherpaOnnx）准备状态 ----

  fun onSpeechEngineChecking() {
    updateVoiceState {
      copy(
        speechModelDownloadProgress = VoiceInputUiState.SPEECH_MODEL_PROGRESS_CHECKING,
        speechEngineReady = false,
      )
    }
  }

  fun onSpeechEngineDownloading(progress: Int) {
    updateVoiceState { copy(speechModelDownloadProgress = progress) }
  }

  fun onSpeechEngineUnzipping() {
    updateVoiceState { copy(speechModelDownloadProgress = VoiceInputUiState.SPEECH_MODEL_PROGRESS_UNZIPPING) }
  }

  fun onSpeechEngineReady() {
    updateVoiceState {
      copy(
        speechModelDownloadProgress = VoiceInputUiState.SPEECH_MODEL_PROGRESS_IDLE,
        speechEngineReady = true,
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    dbObservationJob?.cancel()
    modelObservationJob?.cancel()
  }
}
