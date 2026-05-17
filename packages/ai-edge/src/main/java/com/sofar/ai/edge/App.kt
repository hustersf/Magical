package com.sofar.ai.edge

import android.app.Application
import com.sofar.feature.ai.edge.models.impl.ModelsDataManager

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    ModelsDataManager.init(this)
  }
}