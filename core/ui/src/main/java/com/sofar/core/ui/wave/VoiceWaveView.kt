package com.sofar.core.ui.wave

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.max
import kotlin.math.roundToInt

class VoiceWaveView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val defaultPrimaryColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimary)
  private val defaultSecondaryColor = resolveThemeColor(com.google.android.material.R.attr.colorSecondary)

  private val primaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = defaultPrimaryColor
    strokeCap = Paint.Cap.ROUND
  }

  private val secondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = defaultSecondaryColor
    strokeCap = Paint.Cap.ROUND
  }

  private var level: Float = MIN_LEVEL

  fun setLevel(rmsDB: Float) {
    val target = ((rmsDB - MIN_RMS_DB) / (MAX_RMS_DB - MIN_RMS_DB)).coerceIn(MIN_LEVEL, MAX_LEVEL)
    level = level * SMOOTHING_FACTOR + target * (1f - SMOOTHING_FACTOR)
    invalidate()
  }

  fun reset() {
    level = MIN_LEVEL
    invalidate()
  }

  fun setWaveColors(
    @ColorInt primaryColor: Int,
    @ColorInt secondaryColor: Int = primaryColor
  ) {
    primaryPaint.color = primaryColor
    secondaryPaint.color = secondaryColor
    invalidate()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredWidth = max(
      suggestedMinimumWidth,
      paddingLeft + paddingRight + dpToPx(DEFAULT_CONTENT_WIDTH_DP)
    )
    val desiredHeight = max(
      suggestedMinimumHeight,
      paddingTop + paddingBottom + dpToPx(DEFAULT_CONTENT_HEIGHT_DP)
    )

    val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
    val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
    setMeasuredDimension(measuredWidth, measuredHeight)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val contentLeft = paddingLeft.toFloat()
    val contentTop = paddingTop.toFloat()
    val contentWidth = (width - paddingLeft - paddingRight).coerceAtLeast(0).toFloat()
    val contentHeight = (height - paddingTop - paddingBottom).coerceAtLeast(0).toFloat()
    if (contentWidth <= 0f || contentHeight <= 0f) return

    val centerY = contentTop + contentHeight / 2f
    val barWidth = max(contentWidth / BAR_COUNT / BAR_WIDTH_RATIO, MIN_BAR_WIDTH)
    val gap = ((contentWidth - BAR_COUNT * barWidth) / (BAR_COUNT + 1)).coerceAtLeast(0f)
    primaryPaint.strokeWidth = barWidth
    secondaryPaint.strokeWidth = barWidth

    repeat(BAR_COUNT) { index ->
      val x = contentLeft + gap + barWidth / 2f + index * (barWidth + gap)
      val distanceFromCenter = kotlin.math.abs(index - BAR_COUNT / 2f) / (BAR_COUNT / 2f)
      val weight = 1f - distanceFromCenter * EDGE_ATTENUATION
      val animatedBias = if (index % 2 == 0) EVEN_BAR_BIAS else ODD_BAR_BIAS
      val halfHeight = (contentHeight * (BASE_HEIGHT_RATIO + level * ACTIVE_HEIGHT_RATIO) * weight * animatedBias)
        .coerceAtLeast(barWidth)
      val paint = if (index % 2 == 0) primaryPaint else secondaryPaint
      canvas.drawLine(x, centerY - halfHeight / 2f, x, centerY + halfHeight / 2f, paint)
    }
  }

  private fun resolveThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
  }

  private fun dpToPx(value: Int): Int {
    return (value * resources.displayMetrics.density).roundToInt()
  }

  private companion object {
    private const val DEFAULT_CONTENT_WIDTH_DP = 220
    private const val DEFAULT_CONTENT_HEIGHT_DP = 52
    private const val BAR_COUNT = 13
    private const val BAR_WIDTH_RATIO = 3.6f
    private const val MIN_BAR_WIDTH = 4f
    private const val MIN_RMS_DB = -2f
    private const val MAX_RMS_DB = 10f
    private const val MIN_LEVEL = 0.08f
    private const val MAX_LEVEL = 1f
    private const val SMOOTHING_FACTOR = 0.55f
    private const val BASE_HEIGHT_RATIO = 0.12f
    private const val ACTIVE_HEIGHT_RATIO = 0.72f
    private const val EDGE_ATTENUATION = 0.58f
    private const val EVEN_BAR_BIAS = 1.0f
    private const val ODD_BAR_BIAS = 0.72f
  }
}