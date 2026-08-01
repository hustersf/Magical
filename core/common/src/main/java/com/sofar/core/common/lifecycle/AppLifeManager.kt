package com.sofar.core.common.lifecycle


import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局应用级生命周期与 Activity 堆栈管理器。
 *
 * 提供全应用前后台状态感知及 Activity 运行期堆栈的安全调度能力。
 */
class AppLifeManager private constructor() {

  companion object {
    private const val TAG = "AppLifeManager"

    @JvmStatic
    fun get(): AppLifeManager = Holder.INSTANCE
  }

  private object Holder {
    val INSTANCE = AppLifeManager()
  }

  @Volatile
  private var isAppForeground: Boolean = false

  /**
   * 高并发安全的写时复制集合，用于弱引用持有运行时 Activity 实例。
   */
  private val pages = CopyOnWriteArrayList<WeakReference<Activity>>()

  /**
   * 激活生命周期监听底座。
   * 必须在主应用的 Application 启动时最早时机调用。
   *
   * @param app 全局 Application 上下文
   */
  @MainThread
  fun init(app: Application) {
    val observer = AppLifeObserver()
    app.registerActivityLifecycleCallbacks(observer)
    ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
  }

  /**
   * 获取当前 App 的前后台状态。
   *
   * @return true 表示 App 处于前台可见状态，false 表示处于后台挂起状态。
   */
  fun isAppOnForeground(): Boolean {
    return isAppForeground
  }

  /**
   * 安全获取当前处于栈顶且存活的 Activity 实例。
   *
   * @return 处于栈顶的 Activity 实例，若堆栈为空或已被系统回收则返回 null。
   */
  fun getCurrentActivity(): Activity? {
    if (pages.isEmpty()) {
      return null
    }
    return try {
      pages.lastOrNull()?.get()
    } catch (e: Exception) {
      Log.e(TAG, "getCurrentActivity 捕获越界异常", e)
      null
    }
  }

  /**
   * 一键安全终结并销毁当前堆栈中留存的所有 Activity。
   * 常用于安全退出登录、系统急停、或者强制重启等业务流控场景。
   */
  fun finishAllActivity() {
    for (weakReference in pages) {
      val activity = weakReference.get()
      if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
        activity.finish()
      }
    }
    pages.clear()
  }

  /**
   * 将新建立的 Activity 弱引用推入调度堆栈。
   */
  internal fun addActivity(activity: Activity) {
    pages.add(WeakReference(activity))
    cleanEmptyReferences()
  }

  /**
   * 将已销毁的 Activity 从调度堆栈中安全移除。
   */
  internal fun removeActivity(activity: Activity) {
    val iterator = pages.iterator()
    while (iterator.hasNext()) {
      val wrActivity = iterator.next()
      val act = wrActivity.get()
      if (act == null || act === activity) {
        pages.remove(wrActivity)
      }
    }
  }

  /**
   * 定期自动清空集合中已被 JVM GC 回收的无效弱引用外壳。
   */
  private fun cleanEmptyReferences() {
    pages.removeAll { it.get() == null }
  }

  /**
   * 响应系统级回调：应用切回前台可见状态。
   */
  internal fun onForeground() {
    isAppForeground = true
    Log.d(TAG, "App 状态变更 -> 进入前台")
  }

  /**
   * 响应系统级回调：应用切入后台挂起状态。
   */
  internal fun onBackground() {
    isAppForeground = false
    Log.d(TAG, "App 状态变更 -> 进入后台")
  }
}


