package com.sofar.core.common.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import androidx.annotation.IntRange
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * 将当前位图裁剪并输出为纯圆形位图。
 *
 * 该方法采用 [BitmapShader] 实现，将位图直接作为画笔纹理注入底层，
 * 避免了传统图层混合模式带来的临时对象分配，100% 兼容硬件加速。
 *
 * @return 裁剪后的圆形位图。如果原图已被回收或为空，则返回 null。
 */
fun Bitmap?.toOval(): Bitmap? {
  if (this == null || isRecycled) return null

  val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(output)

  val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    shader = BitmapShader(this@toOval, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
  }

  val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
  canvas.drawOval(rectF, paint)

  return output
}

/**
 * 将当前位图裁剪并输出为带指定圆角半径的位图。
 *
 * @param roundPx 圆角半径（单位：像素/px）。
 * @return 带有圆角的位图。如果原图已被回收或为空，则返回 null。
 */
fun Bitmap?.toRoundRect(roundPx: Float): Bitmap? {
  if (this == null || isRecycled) return null

  val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(output)

  val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    shader = BitmapShader(this@toRoundRect, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
  }

  val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
  canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

  return output
}

/**
 * 对当前位图执行质量压缩，确保其文件大小（KB）严格控制在指定的上限以内。
 *
 * 内部利用 [ByteArrayOutputStream.size] 实时判定流大小，避免了不必要的内存二次拷贝；
 * 配合 Kotlin 标准库的 [use] 代码块，在执行结束后自动释放并关闭底层 I/O 流。
 *
 * @param maxKByteCount 允许的最大千字节数（例如：传入 32 表示限制在 32KB 以内）。
 * @return 压缩成功后的新位图对象。如果发生异常、原图已被回收或为空，则返回 null。
 */
fun Bitmap?.compressToLimit(maxKByteCount: Int): Bitmap? {
  if (this == null || isRecycled) return null
  val maxByteCount = maxKByteCount * 1024L

  return try {
    ByteArrayOutputStream().use { baos ->
      this.compress(Bitmap.CompressFormat.JPEG, 100, baos)
      var option = 90

      while (baos.size() >= maxByteCount && option > 0) {
        baos.reset()
        this.compress(Bitmap.CompressFormat.JPEG, option, baos)
        option -= 10
      }

      ByteArrayInputStream(baos.toByteArray()).use { bais ->
        BitmapFactory.decodeStream(bais, null, null)
      }
    }
  } catch (e: Exception) {
    null
  }
}

/**
 * 将当前位图按照指定的宽高尺寸进行精确缩放。
 *
 * 内部带有智能边界拦截，当目标尺寸与原图尺寸完全一致时，将直接返回原图，零额外开销。
 *
 * @param targetWidth  缩放后的目标宽度。
 * @param targetHeight 缩放后的目标高度。
 * @return 缩放后的新位图，或原位图。如果原图已被回收或为空，则返回 null。
 */
fun Bitmap?.resize(targetWidth: Int, targetHeight: Int): Bitmap? {
  if (this == null || isRecycled) return null
  if (width == targetWidth && height == targetHeight) return this

  val matrix = Matrix().apply {
    postScale(targetWidth.toFloat() / width, targetHeight.toFloat() / height)
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * 将当前位图旋转指定的角度，并输出旋转后的新位图。
 *
 * 内部带有智能边界拦截，当旋转角度为 0° 或 360° 的整倍数时，将直接返回原图，零额外开销。
 *
 * @param degrees 旋转角度（例如：90f、180f、-45f 等）。
 * @return 旋转后的新位图，或原位图。如果原图已被回收或为空，则返回 null。
 */
fun Bitmap?.rotate(degrees: Float): Bitmap? {
  if (this == null || isRecycled) return null
  if (degrees % 360f == 0f) return this

  val matrix = Matrix().apply {
    setRotate(degrees)
  }

  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
}

/**
 * 截取当前位图在目标 [view] 区域内的子位图。
 */
fun Bitmap.getTargetArea(view: View): Bitmap {
  val dstArea = Bitmap.createBitmap(
    view.measuredWidth,
    view.measuredHeight,
    Bitmap.Config.ARGB_8888
  )
  Canvas(dstArea).apply {
    translate(-view.left.toFloat(), -view.top.toFloat())
    drawBitmap(this@getTargetArea, 0f, 0f, null)
  }
  return dstArea
}

/**
 * 截取原始 View ([this]) 在目标 [view] 区域内的位图。
 */
fun View.getTargetAreaOf(view: View): Bitmap? {
  val srcWidth = this.width
  val srcHeight = this.height
  if (srcWidth <= 0 || srcHeight <= 0) return null

  // 先用 Canvas 将原始 View 的当前画面实时绘制到一张标准 Bitmap 上
  val srcBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(srcBitmap)
  this.draw(canvas)

  // 复用上方的 Bitmap 裁剪方法，切出目标区域
  val dstBitmap = srcBitmap.getTargetArea(view)

  // 及时回收临时生成的中间大图，防止内存泄漏
  srcBitmap.recycle()

  return dstBitmap
}

/**
 * 对当前位图进行高斯模糊（默认采用原地模糊模式）。
 */
fun Bitmap.blur(
  context: Context,
  @IntRange(from = 1, to = 25) radius: Int
): Bitmap {
  return this.gaussianBlurInPlace(context, radius)
}

/**
 * 直接在当前位图上进行高斯模糊修改（原地模糊，不创建新位图）。
 * 原 Java: RenderBlur.gaussianBlur(context, original, radius)
 */
fun Bitmap.gaussianBlurInPlace(
  context: Context,
  @IntRange(from = 1, to = 25) radius: Int
): Bitmap {
  val renderScript = RenderScript.create(context)
  try {
    val input = Allocation.createFromBitmap(renderScript, this)
    val output = Allocation.createTyped(renderScript, input.getType())
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

    scriptIntrinsicBlur.apply {
      setRadius(radius.toFloat())
      setInput(input)
      forEach(output)
    }
    output.copyTo(this)
  } finally {
    // 确保安全释放 RenderScript 资源
    renderScript.destroy()
  }
  return this
}

/**
 * 模糊当前位图并返回一张全新位图，同时【强制回收】原位图。
 * 原 Java: RenderBlur.blurBitmap(context, bitmap, radius)
 */
fun Bitmap.blurAndRecycleOriginal(
  context: Context,
  @IntRange(from = 1, to = 25) radius: Int
): Bitmap {
  val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val rs = RenderScript.create(context)
  try {
    val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    val allIn = Allocation.createFromBitmap(rs, this)
    val allOut = Allocation.createFromBitmap(rs, outBitmap)

    blurScript.setRadius(radius.toFloat())
    blurScript.setInput(allIn)
    blurScript.forEach(allOut)
    allOut.copyTo(outBitmap)
  } finally {
    // 确保发生异常时也能安全回收原图和销毁 RenderScript
    this.recycle()
    rs.destroy()
  }
  return outBitmap
}

