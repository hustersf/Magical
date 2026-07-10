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

  val transcript: RecognitionTranscript
    get() = RecognitionTranscript(
      finalSegments = finalSegments.toList(),
      partialText = partialText,
      separator = options.segmentSeparator,
    )

  fun reset(): RecognitionTranscript {
    finalSegments.clear()
    partialText = ""
    return transcript
  }

  fun onPartial(text: String): RecognitionTranscript {
    partialText = text.trim()
    return transcript
  }

  fun onFinal(text: String): RecognitionTranscript {
    val normalizedText = text.trim()
    if (normalizedText.isNotEmpty() && shouldAppendFinalText(normalizedText)) {
      finalSegments += normalizedText
    }
    partialText = ""
    return transcript
  }

  fun complete(): RecognitionTranscript {
    onFinal(partialText)
    return transcript
  }

  private fun shouldAppendFinalText(text: String): Boolean {
    return !options.deduplicateFinalText || finalSegments.lastOrNull() != text
  }
}