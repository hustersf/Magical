package com.sofar.mlkit.core

import android.app.Application
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider

class MLKit private constructor() {

  companion object {
    private val instance: MLKit by lazy {
      MLKit().apply {
        //
      }
    }

    @JvmStatic
    fun get(): MLKit = instance
  }

  fun init(appContext: Application) {
  }

  fun preloadCameraX(context: Context) {
    ProcessCameraProvider.getInstance(context.applicationContext)
  }

}