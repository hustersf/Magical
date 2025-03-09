package com.sofar.mlkit.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class GraphicOverlay @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val lock = Any()
  private val graphics: MutableList<Graphic> = mutableListOf()

  private val transformationMatrix = Matrix()

  private var imageWidth = 0
  private var imageHeight = 0
  private var scaleFactor = 1.0f
  private var postScaleWidthOffset = 0f
  private var postScaleHeightOffset = 0f
  private var isImageFlipped = false
  private var needUpdateTransformation = true


  init {
    addOnLayoutChangeListener { view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
      needUpdateTransformation = true
    }
  }

  fun clear() {
    synchronized(lock) {
      graphics.clear()
    }
    postInvalidate()
  }

  fun add(graphic: Graphic) {
    synchronized(lock) {
      graphics.add(graphic)
    }
  }

  fun remove(graphic: Graphic) {
    synchronized(lock) {
      graphics.remove(graphic)
    }
    postInvalidate()
  }

  fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
    synchronized(lock) {
      this.imageWidth = imageWidth
      this.imageHeight = imageHeight
      this.isImageFlipped = isFlipped
      needUpdateTransformation = true
    }
    postInvalidate()
  }

  fun getImageWidth(): Int {
    return imageWidth
  }

  fun getImageHeight(): Int {
    return imageHeight
  }

  private fun updateTransformationIfNeeded() {
    if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
      return
    }
    val viewAspectRatio = width.toFloat() / height
    val imageAspectRatio = imageWidth.toFloat() / imageHeight
    postScaleWidthOffset = 0f
    postScaleHeightOffset = 0f
    if (viewAspectRatio > imageAspectRatio) {
      // The image needs to be vertically cropped to be displayed in this view.
      scaleFactor = width.toFloat() / imageWidth
      postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
    } else {
      // The image needs to be horizontally cropped to be displayed in this view.
      scaleFactor = height.toFloat() / imageHeight
      postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
    }

    transformationMatrix.reset()
    transformationMatrix.setScale(scaleFactor, scaleFactor)
    transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)

    if (isImageFlipped) {
      transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
    }

    needUpdateTransformation = false
  }


  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    synchronized(lock) {
      updateTransformationIfNeeded()
      for (graphic in graphics) {
        graphic.draw(canvas)
      }
    }
  }


  abstract class Graphic(private val overlay: GraphicOverlay) {

    abstract fun draw(canvas: Canvas)

    protected fun drawRect(
      canvas: Canvas,
      left: Float,
      top: Float,
      right: Float,
      bottom: Float,
      paint: Paint
    ) {
      canvas.drawRect(left, top, right, bottom, paint!!)
    }

    protected fun drawText(canvas: Canvas, text: String?, x: Float, y: Float, paint: Paint?) {
      canvas.drawText(text!!, x, y, paint!!)
    }

    fun scale(imagePixel: Float): Float {
      return imagePixel * overlay.scaleFactor
    }

    fun getApplicationContext(): Context {
      return overlay.context.applicationContext
    }

    fun isImageFlipped(): Boolean {
      return overlay.isImageFlipped
    }

    fun translateX(x: Float): Float {
      return if (overlay.isImageFlipped) {
        overlay.width - (scale(x) - overlay.postScaleWidthOffset)
      } else {
        scale(x) - overlay.postScaleWidthOffset
      }
    }

    fun translateY(y: Float): Float {
      return scale(y) - overlay.postScaleHeightOffset
    }

    val transformationMatrix: Matrix get() = overlay.transformationMatrix

    fun postInvalidate() {
      overlay.postInvalidate()
    }

    fun updatePaintColorByZValue(
      paint: Paint,
      canvas: Canvas,
      visualizeZ: Boolean,
      rescaleZForVisualization: Boolean,
      zInImagePixel: Float,
      zMin: Float,
      zMax: Float
    ) {
      if (!visualizeZ) {
        return
      }

      // When visualizeZ is true, sets up the paint to different colors based on z values.
      // Gets the range of z value.
      val zLowerBoundInScreenPixel: Float
      val zUpperBoundInScreenPixel: Float

      if (rescaleZForVisualization) {
        zLowerBoundInScreenPixel = min(-0.001, scale(zMin).toDouble()).toFloat()
        zUpperBoundInScreenPixel = max(0.001, scale(zMax).toDouble()).toFloat()
      } else {
        // By default, assume the range of z value in screen pixel is [-canvasWidth, canvasWidth].
        val defaultRangeFactor = 1f
        zLowerBoundInScreenPixel = -defaultRangeFactor * canvas.width
        zUpperBoundInScreenPixel = defaultRangeFactor * canvas.width
      }

      val zInScreenPixel = scale(zInImagePixel)

      if (zInScreenPixel < 0) {
        // Sets up the paint to be red if the item is in front of the z origin.
        // Maps values within [zLowerBoundInScreenPixel, 0) to [255, 0) and use it to control the
        // color. The larger the value is, the more red it will be.
        var v = (zInScreenPixel / zLowerBoundInScreenPixel * 255).toInt()
        v = v.coerceIn(0, 255)
        paint.setARGB(255, 255, 255 - v, 255 - v)
      } else {
        // Sets up the paint to be blue if the item is behind the z origin.
        // Maps values within [0, zUpperBoundInScreenPixel] to [0, 255] and use it to control the
        // color. The larger the value is, the more blue it will be.
        var v = (zInScreenPixel / zUpperBoundInScreenPixel * 255).toInt()
        v = v.coerceIn(0, 255)
        paint.setARGB(255, 255 - v, 255 - v, 255)
      }
    }
  }

}