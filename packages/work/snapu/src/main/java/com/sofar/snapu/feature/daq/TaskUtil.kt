package com.sofar.snapu.feature.daq

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.sofar.snapu.feature.daq.model.Product
import com.sofar.utility.io.FileUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object TaskUtil {

  const val KET_TASK_ID = "task_id"
  const val VIDEO_DURATION_SECONDS = 10

  private const val PRODUCT_ROOT: String = "product"
  private const val SHELF_ROOT: String = "shelf"
  private const val TEXT_ROOT: String = "text"

  private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

  /**
   * 任务唯一标识
   */
  fun buildTaskId(): Long {
    return System.currentTimeMillis()
  }

  fun productRootFile(context: Context): File {
    val file = File(CaptureFileUtil.mkdirs(context, PRODUCT_ROOT))
    return file
  }

  fun productTextRootFile(context: Context): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTextDir()))
    return file
  }

  /**
   * 商品文件存储目录
   * product/date/taskId
   */
  fun productTaskDir(taskId: Long): String {
    return "$PRODUCT_ROOT/${formatDate(taskId)}/$taskId/"
  }

  fun productTextDir(): String {
    return "$PRODUCT_ROOT/$TEXT_ROOT"
  }

  /**
   * 商品文件存储目录
   * shelf/date/taskId
   */
  fun shelfTaskDir(taskId: Long): String {
    return "$SHELF_ROOT/${formatDate(taskId)}/$taskId/"
  }

  fun shelfTextDir(): String {
    return "$SHELF_ROOT/$TEXT_ROOT"
  }

  fun getProductTaskFile(context: Context, taskId: Long): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTaskDir(taskId)))
    return file
  }

  fun getProductPhotoFile(context: Context, taskId: Long): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTaskDir(taskId)), photoName(taskId))
    return file
  }

  fun getProductVideoFile(context: Context, taskId: Long): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTaskDir(taskId)), videoName(taskId))
    return file
  }

  fun getProductVideoFrameFile(context: Context, taskId: Long): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTaskDir(taskId)), videoFrameName(taskId))
    return file
  }

  fun getProductTextFile(context: Context, taskId: Long): File {
    val file = File(CaptureFileUtil.mkdirs(context, productTextDir()), textName(taskId))
    return file
  }

  fun photoName(taskId: Long): String {
    return "photo_${taskId}.png"
  }

  fun videoName(taskId: Long): String {
    return "video_${taskId}.mp4"
  }

  fun videoFrameName(taskId: Long): String {
    return "video_frame_${taskId}.png"
  }

  fun textName(taskId: Long): String {
    return "text_${taskId}.json"
  }

  private fun formatDate(taskId: Long): String {
    val date = Date(taskId)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedDate = sdf.format(date)
    return formattedDate
  }

  fun formatDateInSecond(taskId: Long): String {
    val date = Date(taskId)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val formattedDate = sdf.format(date)
    return formattedDate
  }

  @WorkerThread
  fun writeProduct(context: Context, product: Product) {
    val file = getProductTextFile(context, product.taskId)
    val str = Gson().toJson(product)
    CaptureFileUtil.writeText(file, str)
  }

  @WorkerThread
  fun readProduct(file: File): Product {
    val str = CaptureFileUtil.readText(file)
    return Gson().fromJson(str, Product::class.java)
  }

  @WorkerThread
  fun deleteProduct(context: Context, product: Product) {
    val textFile = getProductTextFile(context, product.taskId)
    val file = getProductTaskFile(context, product.taskId)
    FileUtil.deleteQuietly(file)
    FileUtil.deleteQuietly(textFile)
  }

  fun writeProductAsync(context: Context, product: Product, runnable: Runnable? = null) {
    executorService.execute {
      writeProduct(context, product)
      runnable?.run()
    }
  }

  fun deleteProductAsync(context: Context, product: Product, runnable: Runnable? = null) {
    executorService.execute {
      deleteProduct(context, product)
      runnable?.run()
    }
  }
}