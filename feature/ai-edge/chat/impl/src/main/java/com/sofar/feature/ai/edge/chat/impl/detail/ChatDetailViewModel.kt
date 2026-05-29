package com.sofar.feature.ai.edge.chat.impl.detail

import androidx.lifecycle.ViewModel
import com.sofar.core.ai.edge.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
  private val repository: ChatRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChatDetailUiState())
  val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

}