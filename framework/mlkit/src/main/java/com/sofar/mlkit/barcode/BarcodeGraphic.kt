package com.sofar.mlkit.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.barcode.common.Barcode
import com.sofar.mlkit.core.GraphicOverlay
import kotlin.math.max
import kotlin.math.min

class BarcodeGraphic(overlay: GraphicOverlay, private val barcode: Barcode) :
  GraphicOverlay.Graphic(overlay) {
  private val rectPaint: Paint = Paint()

  init {
    rectPaint.color = MARKER_COLOR
    rectPaint.style = Paint.Style.STROKE
    rectPaint.strokeWidth = STROKE_WIDTH
  }

  override fun draw(canvas: Canvas) {
    // Draws the bounding box around the BarcodeBlock.
    val rect = RectF(barcode.boundingBox)
    // If the image is flipped, the left will be translated to right, and the right to left.
    val x0 = translateX(rect.left)
    val x1 = translateX(rect.right)
    rect.left = min(x0, x1)
    rect.right = max(x0, x1)
    rect.top = translateY(rect.top)
    rect.bottom = translateY(rect.bottom)
    canvas.drawRect(rect, rectPaint)
  }

  companion object {
    private const val MARKER_COLOR = Color.WHITE
    private const val STROKE_WIDTH = 4.0f
  }
}
