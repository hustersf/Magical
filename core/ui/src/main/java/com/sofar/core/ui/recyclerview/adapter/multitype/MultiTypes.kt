package com.sofar.core.ui.recyclerview.adapter.multitype

class MultiTypes {

  private val types = mutableListOf<Type>()

  fun register(type: Type) {
    types.add(type)
  }

  fun firstIndex(clazz: Class<*>): Int {
    // 使用 Kotlin 极其高效的集合操作函数，替代原有的 for 循环查找
    return types.indexOfFirst { it.mClass == clazz }
  }

  fun getType(index: Int): Type {
    return types[index]
  }
}
