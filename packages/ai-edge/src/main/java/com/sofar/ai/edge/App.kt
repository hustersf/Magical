package com.sofar.ai.edge

import android.app.Application
import com.sofar.core.ai.edge.data.repository.ModelsDataManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    ModelsDataManager.init(this)
  }
}