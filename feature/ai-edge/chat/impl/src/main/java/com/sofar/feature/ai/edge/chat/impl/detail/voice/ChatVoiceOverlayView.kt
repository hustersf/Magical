package com.sofar.feature.ai.edge.chat.impl.detail.voice

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.sofar.core.ui.wave.VoiceWaveView
import com.sofar.core.ui.R as coreUiR
import com.sofar.feature.ai.edge.chat.api.R as chatR

class ChatVoiceOverlayView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private val cancelText = TextView(context)
  private val waveView = VoiceWaveView(context)
  private val overlayText = TextView(context)
  private var wasVisible: Boolean = false

  init {
    orientation = VERTICAL
    addCancelText()
    addWaveView()
    addOverlayText()
  }

  fun render(state: VoiceInputUiState) {
    val visible = state.isVoiceOverlayVisible
    visibility = if (visible) VISIBLE else GONE

    if (!visible) {
      if (wasVisible) {
        waveView.reset()
      }
      wasVisible = false
      return
    }

    if (!wasVisible) {
      waveView.reset()
    }
    wasVisible = true

    cancelText.text = context.getString(
      if (state.isVoiceCanceling) {
        chatR.string.feature_chat_voice_release_to_cancel
      } else {
        chatR.string.feature_chat_voice_slide_up_to_cancel
      }
    )
    overlayText.text = when {
      state.voiceRecognizedText.isNotEmpty() -> state.voiceRecognizedText
      else -> context.getString(chatR.string.feature_chat_voice_listening)
    }

    if (state.isVoiceCanceling) {
      waveView.setWaveColors(
        primaryColor = resolveThemeColor(com.google.android.material.R.attr.colorError),
        secondaryColor = resolveThemeColor(com.google.android.material.R.attr.colorErrorContainer)
      )
    } else {
      waveView.setWaveColors(
        primaryColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimary),
        secondaryColor = resolveThemeColor(com.google.android.material.R.attr.colorSecondary)
      )
    }
    waveView.setLevel(state.voiceRmsDB)
  }

  private fun resolveThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
  }

  private fun addCancelText() {
    cancelText.apply {
      layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
      gravity = Gravity.CENTER
      textAlignment = TEXT_ALIGNMENT_CENTER
      setTextAppearance(coreUiR.style.CoreUiTextAppearance_Caption)
    }
    addView(cancelText)
  }

  private fun addWaveView() {
    waveView.layoutParams =
      LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
        topMargin = resources.getDimensionPixelSize(coreUiR.dimen.core_ui_spacing_sm)
      }
    addView(waveView)
  }

  private fun addOverlayText() {
    overlayText.apply {
      layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
        topMargin = resources.getDimensionPixelSize(coreUiR.dimen.core_ui_spacing_sm)
      }
      gravity = Gravity.CENTER
      textAlignment = TEXT_ALIGNMENT_CENTER
      ellipsize = TextUtils.TruncateAt.END
      maxLines = 2
      setTextAppearance(coreUiR.style.CoreUiTextAppearance_Body)
    }
    addView(overlayText)
  }
}