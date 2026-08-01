package com.sofar.core.common.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal class AppLifeObserver : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    AppLifeManager.get().addActivity(activity)
  }

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityResumed(activity: Activity) {}

  override fun onActivityPaused(activity: Activity) {}

  override fun onActivityStopped(activity: Activity) {}

  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

  override fun onActivityDestroyed(activity: Activity) {
    AppLifeManager.get().removeActivity(activity)
  }

  override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    when (event) {
      Lifecycle.Event.ON_START -> AppLifeManager.get().onForeground()
      Lifecycle.Event.ON_STOP -> AppLifeManager.get().onBackground()
      else -> { /* 忽略其他生命周期事件 */
      }
    }
  }
}
