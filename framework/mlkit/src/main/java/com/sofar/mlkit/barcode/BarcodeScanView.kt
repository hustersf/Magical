package com.sofar.mlkit.barcode

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.sofar.mlkit.R

class BarcodeScanView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private var animator: ValueAnimator? = null
  private var scanLineY = 0f
  private var scanSpeed: Int = 2000
  private var scanArea = RectF()
  private var scanLineColor: Int = Color.GREEN
  private var scanLineDrawable: Drawable? = null

  // 使用Material 3主题颜色
  private val defaultLineColor: Int
    get() {
      val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.colorPrimary))
      val color = a.getColor(0, Color.GREEN)
      a.recycle()
      return color
    }

  init {
    scanLineDrawable = ContextCompat.getDrawable(context, R.drawable.scan_line_default)?.mutate()
    scanArea.set(getAnalysisArea())
    setupAttributes(attrs)
    setupAnimator()
  }

  private fun setupAttributes(attrs: AttributeSet?) {
    context.obtainStyledAttributes(
      attrs,
      R.styleable.BarcodeScanView,
      0,
      0
    ).apply {
      try {
        scanSpeed = getInt(
          R.styleable.BarcodeScanView_scanSpeed,
          2000
        )
        scanLineColor = getColor(
          R.styleable.BarcodeScanView_scanLineColor,
          defaultLineColor
        )
        if (hasValue(R.styleable.BarcodeScanView_scanLineDrawable)) {
          scanLineDrawable = getDrawable(R.styleable.BarcodeScanView_scanLineDrawable)
        }
        if (hasValue(R.styleable.BarcodeScanView_scanLineColor)) {
          scanLineDrawable?.colorFilter =
            PorterDuffColorFilter(scanLineColor, PorterDuff.Mode.SRC_IN)
        }
      } finally {
        recycle()
      }
    }
  }

  private fun setupAnimator() {
    animator = ValueAnimator.ofFloat(0f, 1f).apply {
      duration = scanSpeed.toLong()
      repeatCount = ValueAnimator.INFINITE
      repeatMode = ValueAnimator.RESTART
      interpolator = LinearInterpolator()
      addUpdateListener {
        val fraction = it.animatedFraction
        scanLineY = height * scanArea.top + height * (scanArea.bottom - scanArea.top) * fraction
        invalidate()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    animator?.start()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    animator?.cancel()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    scanLineDrawable?.let {
      val w = it.intrinsicWidth
      val h = it.intrinsicHeight
      val lineWidth = width * (scanArea.right - scanArea.left)
      val lineHeight = lineWidth * h / w
      it.setBounds(
        0,
        0,
        lineWidth.toInt(),
        lineHeight.toInt()
      )

      // 保存画布状
      val saveCount = canvas.save()
      // 平移到扫描区域
      canvas.translate(
        width * scanArea.left, // X 起始位置保持原逻辑
        scanLineY - lineHeight / 2 // 垂直居中
      )
      // 绘制扫描线图片
      it.draw(canvas)
      // 恢复画布状态
      canvas.restoreToCount(saveCount)
    }
  }

  private val Int.dp: Float
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      this.toFloat(),
      resources.displayMetrics
    )

  // 获取扫描框在 View 中的相对位置（标准化 0-1）
  fun getAnalysisArea(): RectF {
    return RectF(0.05f, 0.15f, 0.95f, 0.7f)
  }
}

