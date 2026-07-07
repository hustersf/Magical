package com.sofar.core.ui.progressbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withSave
import com.sofar.core.ui.R
import com.sofar.core.ui.util.dp2px

class SegmentedProgressBar @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  companion object {
    private const val DEFAULT_CONTENT_HEIGHT_DP = 6f
    private const val DEFAULT_SEGMENT_GAP_DP = 1f
    private const val CORNER_RADIUS_RATIO = 0.5f
    private const val DEFAULT_ANIM_DURATION = 400L
    private val ANIM_INTERPOLATOR = PathInterpolator(0.05f, 0.7f, 0.1f, 1.0f)
  }

  /**
   * 分段进度数据。
   *
   * @param percentage 当前片段占整体宽度的比例，取值范围为 [0f, 1f]。
   *                   本控件不会校验输入数据，调用方需保证：
   *                   1. percentage >= 0f
   *                   2. percentage <= 1f
   *                   3. 所有 Segment 的 percentage 总和 <= 1f
   * @param color Android Color Int。
   */
  data class Segment(val percentage: Float, val color: Int)

  private class AnimatableSegment(
    val color: Int,
    var startPercentage: Float,
    var endPercentage: Float,
    var currentPercentage: Float
  )

  private val runtimeSegments = mutableListOf<AnimatableSegment>()
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val path = Path()
  private val rectF = RectF()

  private var animator: ValueAnimator? = null
  private var colorEmpty = ContextCompat.getColor(context, R.color.core_ui_color_progress_track)
  private var segmentGap = context.dp2px(DEFAULT_SEGMENT_GAP_DP)
  private val defaultContentHeight = context.dp2px(DEFAULT_CONTENT_HEIGHT_DP).toInt()

  private var pendingSegments: List<Segment>? = null
  private var pendingDuration: Long = DEFAULT_ANIM_DURATION
  private val pendingLaunchRunnable = Runnable { launchPendingSegmentsIfReady() }

  init {
    if (attrs != null) {
      context.withStyledAttributes(attrs, R.styleable.SegmentedProgressBar, defStyleAttr, 0) {
        segmentGap = getDimension(
          R.styleable.SegmentedProgressBar_pb_segmentGap,
          segmentGap
        )
        colorEmpty = getColor(
          R.styleable.SegmentedProgressBar_pb_colorEmpty,
          colorEmpty
        )
      }
    }
  }

  /**
   * 更新分段数据并播放过渡动画。
   *
   * 注意：
   * 1. [segments] 会按下标与上一次数据匹配，请保持同一业务类型的顺序稳定。
   * 2. 调用方需保证 [Segment.percentage] 和 [Segment.color] 合法，控件不会在绘制阶段过滤非法数据。
   * 3. 如果调用时 View 尚未 attach 或尺寸尚未就绪，控件会缓存最后一次数据，并在可绘制后自动启动动画。
   */
  fun updateSegments(
    segments: List<Segment>,
    duration: Long = DEFAULT_ANIM_DURATION
  ) {
    val segmentSnapshot = segments.toList()

    if (!canLaunchAnimation()) {
      pendingSegments = segmentSnapshot
      pendingDuration = duration
      removeCallbacks(pendingLaunchRunnable)
      return
    }

    clearPendingLaunch()
    animateSegments(segmentSnapshot, duration)
  }

  private fun animateSegments(segments: List<Segment>, duration: Long) {
    animator?.cancel()

    val updatedRuntimeList = mutableListOf<AnimatableSegment>()
    val maxCount = maxOf(runtimeSegments.size, segments.size)

    for (i in 0 until maxCount) {
      val oldSeg = runtimeSegments.getOrNull(i)
      val newSeg = segments.getOrNull(i)

      val startVal = oldSeg?.currentPercentage ?: 0f
      val endVal = newSeg?.percentage ?: 0f

      if (newSeg != null || oldSeg != null) {
        updatedRuntimeList.add(
          AnimatableSegment(
            color = newSeg?.color ?: (oldSeg?.color ?: Color.TRANSPARENT),
            startPercentage = startVal,
            endPercentage = endVal,
            currentPercentage = startVal
          )
        )
      }
    }

    runtimeSegments.clear()
    runtimeSegments.addAll(updatedRuntimeList)

    animator = ValueAnimator.ofFloat(0f, 1f).apply {
      this.duration = duration
      this.interpolator = ANIM_INTERPOLATOR
      addUpdateListener { animation ->
        val fraction = animation.animatedValue as Float
        for (segment in runtimeSegments) {
          segment.currentPercentage = segment.startPercentage +
              (segment.endPercentage - segment.startPercentage) * fraction
        }
        invalidate()
      }
      start()
    }
  }

  private fun launchPendingSegmentsIfReady() {
    val segments = pendingSegments ?: return
    if (!canLaunchAnimation()) {
      return
    }

    val duration = pendingDuration
    pendingSegments = null
    animateSegments(segments, duration)
  }

  private fun clearPendingLaunch() {
    removeCallbacks(pendingLaunchRunnable)
    pendingSegments = null
  }

  private fun canLaunchAnimation(): Boolean {
    return isAttachedToWindow && hasValidContentSize()
  }

  private fun postPendingLaunchIfReady() {
    if (canLaunchAnimation() && pendingSegments != null) {
      removeCallbacks(pendingLaunchRunnable)
      post(pendingLaunchRunnable)
    }
  }

  private fun hasValidContentSize(): Boolean {
    return width > paddingLeft + paddingRight && height > paddingTop + paddingBottom
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val contentWidth = width.toFloat() - paddingLeft - paddingRight
    val contentHeight = height.toFloat() - paddingTop - paddingBottom
    if (contentWidth <= 0 || contentHeight <= 0) {
      return
    }
    val radius = contentHeight * CORNER_RADIUS_RATIO

    canvas.withSave {
      translate(paddingLeft.toFloat(), paddingTop.toFloat())

      // 默认绘制底层基础灰色
      paint.color = colorEmpty
      rectF.set(0f, 0f, contentWidth, contentHeight)
      drawRoundRect(rectF, radius, radius, paint)

      if (runtimeSegments.isEmpty()) return@withSave

      // 裁剪圆角，确保即使两端缩进，首尾外侧轮廓依然是圆角
      path.reset()
      path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
      clipPath(path)

      // 顺次拼接绘制各个色块
      var currentLeft = 0f

      for (index in runtimeSegments.indices) {
        val segment = runtimeSegments[index]
        val segmentWidth = contentWidth * segment.currentPercentage // 使用内容净宽度计算

        val isLast = index == runtimeSegments.lastIndex
        // 只有当前色块宽度足够时才应用 Gap
        val gapAdjustment = if (!isLast && segmentWidth > segmentGap) {
          segmentGap
        } else {
          0f
        }

        val currentRight = currentLeft + segmentWidth
        val drawRight = currentRight - gapAdjustment

        // 确保缩进后依然是一个合理的正向矩形
        if (drawRight > currentLeft) {
          paint.color = segment.color
          drawRect(currentLeft, 0f, drawRight, contentHeight, paint)
        }

        // 下一段的起点必须严格承接没有缩进前的物理真实右边界
        currentLeft = currentRight
      }
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredHeight = defaultContentHeight + paddingTop + paddingBottom
    val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
    val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
    setMeasuredDimension(measuredWidth, measuredHeight)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    postPendingLaunchIfReady()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    postPendingLaunchIfReady()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    animator?.cancel()
    animator?.removeAllUpdateListeners()
    animator = null
    clearPendingLaunch()
  }
}