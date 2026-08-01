package com.sofar.core.ui.recyclerview.adapter.multitype

import com.sofar.core.ui.recyclerview.adapter.Cell
import com.sofar.core.ui.recyclerview.adapter.CellAdapter

/**
 * 支持不同数据类型的列表
 */
class MultiTypeAdapter : CellAdapter<Any>() {

  private val mTypes = MultiTypes()

  /**
   * 一对一关系
   *
   * @param clazz   数据类型
   * @param factory 一种数据类型对应一种 factory
   */
  fun <T> register(clazz: Class<T>, factory: CellFactory<T>) {
    mTypes.register(Type(clazz, factory, DefaultLinker()))
  }

  /**
   * 一对多关系
   *
   * @param clazz     数据类型
   * @param factories 一种数据类型对应多个
   * @param linker    根据数据从 factories 中找出相应的 factory
   */
  fun <T> register(clazz: Class<T>, factories: List<CellFactory<T>>, linker: Linker<T>) {
    factories.forEach { factory ->
      mTypes.register(Type(clazz, factory, linker))
    }
  }

  override fun onCreateCell(viewType: Int): Cell<Any> {
    // 由于 Type 内使用了星投影，这里需要通过不检查的转型将 Cell<*> 转换为基类所需的 Cell<Any>
    @Suppress("UNCHECKED_CAST")
    return mTypes.getType(viewType).mFactory.onCreateCell() as Cell<Any>
  }

  override fun getItemViewType(position: Int): Int {
    return indexOfType(position)
  }

  private fun indexOfType(position: Int): Int {
    // 这里的 item 是 Any? 类型
    val item =
      getItem(position) ?: throw IllegalStateException("Item at position $position is null")
    val index = mTypes.firstIndex(item.javaClass)

    if (index != -1) {
      // 通过无安全检查转型，将不可控泛型的 Linker 强转为安全接收该 item 的基础 Linker
      @Suppress("UNCHECKED_CAST")
      val linker = mTypes.getType(index).mLinker as Linker<Any>
      return index + linker.index(position, item)
    }
    throw IllegalStateException("you must register class=${item.javaClass}")
  }
}
