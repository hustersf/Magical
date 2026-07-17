package com.sofar.core.speech.internal.transcript

internal data class RecognitionTranscript(
  val finalSegments: List<String> = emptyList(),
  val partialText: String = "",
  val separator: String = DEFAULT_SEPARATOR,
) {
  val text: String
    get() = (finalSegments + partialText.trim())
      .filter { it.isNotEmpty() }
      .joinToString(separator = separator)

  val hasText: Boolean
    get() = finalSegments.isNotEmpty() || partialText.isNotBlank()

  companion object {
    const val DEFAULT_SEPARATOR = "  ·  "
  }
}

internal data class TranscriptOptions(
  val segmentSeparator: String = RecognitionTranscript.DEFAULT_SEPARATOR,
  val deduplicateFinalText: Boolean = false,
)

internal class RecognitionTranscriptAggregator(
  private val options: TranscriptOptions = TranscriptOptions(),
) {
  private val finalSegments = mutableListOf<String>()
  private var partialText: String = ""

  // 当前聚合状态的快照（分段由 separator 连接，部分文本追加在末尾）
  val transcript: RecognitionTranscript
    get() = RecognitionTranscript(
      finalSegments = finalSegments.toList(),
      partialText = partialText,
      separator = options.segmentSeparator,
    )

  // 清空分段和部分文本
  fun reset(): RecognitionTranscript {
    finalSegments.clear()
    partialText = ""
    return transcript
  }

  // 更新部分文本（实时显示中间结果）
  fun onPartial(text: String): RecognitionTranscript {
    partialText = text.trim()
    return transcript
  }

  // 追加最终文本段（去重由 options.deduplicateFinalText 控制）
  fun onFinal(text: String): RecognitionTranscript {
    val normalizedText = text.trim()
    if (normalizedText.isNotEmpty() && shouldAppendFinalText(normalizedText)) {
      finalSegments += normalizedText
    }
    partialText = ""
    return transcript
  }

  // 最后一次聚合：部分文本转为最终段
  fun complete(): RecognitionTranscript {
    onFinal(partialText)
    return transcript
  }

  private fun shouldAppendFinalText(text: String): Boolean {
    return !options.deduplicateFinalText || finalSegments.lastOrNull() != text
  }
}