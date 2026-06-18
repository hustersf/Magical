package com.sofar.core.common.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

/**
 * 图片压缩工具类
 */
object ImageCompressor {

  private const val TAG = "ImageCompressor"

  /**
   * 根据路径同步解码并压缩图片
   *
   * @param filePath 图片本地绝对路径
   * @param maxEdge 限制的最大长边像素（默认1024，适合端侧大模型输入或常规预览）
   * @param config 颜色通道配置（默认RGB_565，无透明度，比ARGB_8888省50%内存）
   * @return 压缩后的 Bitmap，如果文件不存在或解码失败则返回 null
   */
  fun compressImageFile(
    filePath: String,
    maxEdge: Int = 1024,
    config: Bitmap.Config = Bitmap.Config.RGB_565
  ): Bitmap? {
    val file = File(filePath)
    if (!file.exists()) {
      Log.d(TAG, "compressImageFile: 文件不存在 -> $filePath")
      return null
    }

    return try {
      val options = BitmapFactory.Options().apply {
        // Step 1: 仅读取图片边界信息（不分配内存）
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeFile(file.absolutePath, options)

      // Step 2: 计算最接近目标尺寸的下采样比例 (Power of 2)
      options.inSampleSize = calculateInSampleSize(options, maxEdge, maxEdge)
      options.inJustDecodeBounds = false

      // Step 3: 设置颜色通道
      options.inPreferredConfig = config

      // 正式解码图片到内存
      val sampledBitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null

      // Step 4: 精确等比缩放，确保长边严格不超过 maxEdge
      val finalBitmap = scaleBitmapToFit(sampledBitmap, maxEdge)

      // Step 5: 沉淀到独立的 log 方法中（随时可删）
      printReportLog(file, options, finalBitmap, config)

      finalBitmap
    } catch (e: Exception) {
      Log.d(TAG, "compressImageFile 失败: $filePath", e)
      null
    }
  }

  /**
   * 【随时可删的独立日志方法】
   * 负责所有复杂的耗时计算与字符串拼接，主方法只需单行调用。
   */
  private fun printReportLog(
    file: File,
    options: BitmapFactory.Options,
    finalBitmap: Bitmap,
    config: Bitmap.Config
  ) {
    try {
      // 根据传入的 config 动态计算单像素字节数 (RGB_565为2字节，ARGB_8888为4字节)
      val bytesPerPixel = when (config) {
        Bitmap.Config.RGB_565 -> 2
        Bitmap.Config.ALPHA_8 -> 1
        else -> 4 // 包含 ARGB_8888 和硬件位图等默认情况
      }

      val origWidth = options.outWidth
      val origHeight = options.outHeight
      // 按实际选择的色彩通道计算直解内存
      val origMemorySizeBytes = origWidth * origHeight * bytesPerPixel.toFloat()
      val origMemorySizeMb = origMemorySizeBytes / (1024f * 1024f)
      val fileSizeBytes = file.length() / 1024f

      val finalWidth = finalBitmap.width
      val finalHeight = finalBitmap.height
      val finalMemorySizeBytes = finalBitmap.byteCount
      val finalMemorySizeMb = finalMemorySizeBytes / (1024f * 1024f)

      // 分子分母基准对齐，计算真实的内存缩减比例
      val optimizationRate = (1 - (finalMemorySizeBytes / origMemorySizeBytes)) * 100

      Log.d(
        TAG, """
            |================ [图片压缩报告] ================
            | 文件路径: ${file.name} (磁盘大小: ${String.format("%.2f", fileSizeBytes)} KB)
            | ----------------------------------------------
            | 压缩前 (若直解): 尺寸 = ${origWidth}x${origHeight}, 预估内存 = ${
          String.format(
            "%.2f",
            origMemorySizeMb
          )
        } MB (${config.name})
            | 压缩后 (实际进): 尺寸 = ${finalWidth}x${finalHeight}, 实际内存 = ${
          String.format(
            "%.2f",
            finalMemorySizeMb
          )
        } MB (${finalBitmap.config?.name ?: "UNKNOWN"})
            | 优化率: 内存占用降低了 ${String.format("%.2f", optimizationRate)}%
            |================================================
        """.trimMargin()
      )
    } catch (e: Exception) {
      // 规避日志打印本身的空指针或计算异常，确保不影响主业务
    }
  }

  /**
   * 计算内存下采样的比例 (inSampleSize)
   */
  private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
  ): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
      val halfHeight = height / 2
      val halfWidth = width / 2
      // 循环计算，保证采样后的宽高依然大于或接近目标宽高
      while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  /**
   * 精确等比缩放 Bitmap，并安全释放中间产生的旧 Bitmap 内存
   */
  private fun scaleBitmapToFit(bitmap: Bitmap, maxEdge: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= maxEdge && height <= maxEdge) return bitmap

    val ratio = width.toFloat() / height.toFloat()
    val (newWidth, newHeight) = if (width > height) {
      maxEdge to (maxEdge / ratio).toInt()
    } else {
      (maxEdge * ratio).toInt() to maxEdge
    }

    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    if (scaledBitmap != bitmap) {
      bitmap.recycle() // 及时显式回收旧的临时大图，防内存抖动
    }
    return scaledBitmap
  }
}
