package com.sofar.core.ai.edge.data.entity.chat

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatDetailArgs(
  val sessionId: String? = null,
  val agentId: String? = null,
) : Parcelable