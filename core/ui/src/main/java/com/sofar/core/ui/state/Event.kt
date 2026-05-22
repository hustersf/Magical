package com.sofar.core.ui.state

class Event<out T>(private val content: T) {

  // 标记该事件是否已被消费
  var hasBeenHandled = false
    private set

  /**
   * 获取内容。如果已经被消费过，则返回 null
   */
  fun getContentIfNotHandled(): T? {
    return if (hasBeenHandled) {
      null
    } else {
      hasBeenHandled = true
      content
    }
  }

  /**
   * 仅查看内容，不消耗它（通常用于单元测试中的断言）
   */
  fun peekContent(): T = content
}

/**
 * 方便 View 层安全消费事件的 Kotlin 扩展函数
 */
inline fun <T> Event<T>?.observeEvent(action: (T) -> Unit) {
  this?.getContentIfNotHandled()?.let(action)
}