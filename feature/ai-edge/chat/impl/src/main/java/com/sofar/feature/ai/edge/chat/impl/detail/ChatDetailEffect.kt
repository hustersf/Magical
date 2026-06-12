package com.sofar.feature.ai.edge.chat.impl.detail

sealed interface ChatDetailEffect {
  data class ShowToast(val message: String) : ChatDetailEffect
  data class ShowAlert(val message: String) : ChatDetailEffect
}