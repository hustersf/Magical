package com.sofar.ai.edge.compose

import android.app.Application
import com.sofar.core.ai.edge.domain.usecase.InitModelConfigUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

  @Inject
  lateinit var initModelConfigUseCase: InitModelConfigUseCase

  override fun onCreate() {
    super.onCreate()
    initModelConfigUseCase()
  }
}
