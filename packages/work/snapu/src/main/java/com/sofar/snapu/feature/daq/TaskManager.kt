package com.sofar.snapu.feature.daq

import android.content.Context
import com.sofar.snapu.SofarApp
import com.sofar.snapu.feature.daq.model.Product
import com.sofar.snapu.feature.daq.model.Shelf
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TaskManager private constructor() {

  private val executorService: ExecutorService = Executors.newCachedThreadPool()

  private lateinit var context: Context
  private var product: Product = Product()
  private var shelf: Shelf = Shelf()
  private var edit = false

  companion object {
    private val instance: TaskManager by lazy {
      TaskManager().apply {
        context = SofarApp.getAppContext()
      }
    }

    @JvmStatic
    fun get(): TaskManager = instance
  }

  fun isEdit(): Boolean {
    return edit
  }

  fun productStart(taskId: Long, edit: Boolean = false) {
    product.taskId = taskId
    product.uploaded = false
    if (edit) {
      product.version++
    }
    this.edit = edit
  }

  fun productEAN(ean: String, name: String, path: String) {
    product.ean = ean
    product.name = name
    product.imageFile = path
  }

  fun productVideo(path: String) {
    product.videoFile = path
  }

  fun productVideoFrame(path: String) {
    product.videoFrameFile = path
  }

  fun productSave() {
    executorService.execute {
      TaskUtil.writeProduct(context, product)
    }
  }

  fun productSubmit() {
    product.uploaded = true
    productSave()
  }

}