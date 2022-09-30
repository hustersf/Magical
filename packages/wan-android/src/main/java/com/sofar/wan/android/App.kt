package com.sofar.wan.android

import android.app.Application

class App : Application() {

  companion object {
    private lateinit var theApp: App
    fun getAppContext(): App {
      return theApp
    }
  }


  override fun onCreate() {
    super.onCreate()
    theApp = this
  }
}