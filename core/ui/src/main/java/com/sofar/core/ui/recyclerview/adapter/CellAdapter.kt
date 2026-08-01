package com.sofar.core.ui.recyclerview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class CellAdapter<T> : RecyclerView.Adapter<CellViewHolder<T>>() {

  // 存储所有的 Cell，使用星投影 Cell<*> 允许容纳任何泛型的 Cell 实例
  private val mCells = mutableListOf<Cell<*>>()
  private val mList = mutableListOf<T>()

  // 使用属性的 Getter/Setter 机制替代 getItems() 和 setItems()
  var items: List<T>
    get() = mList
    set(value) {
      mList.clear()
      mList.addAll(value)
    }

  // 局部修改轨（影子属性）：提供可变权限。
  val mutableItems: MutableList<T>
    get() = mList

  protected abstract fun onCreateCell(viewType: Int): Cell<T>

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder<T> {
    val cell = onCreateCell(viewType)
    val itemView = cell.createView(parent)
    mCells.add(cell)
    return CellViewHolder(itemView, cell)
  }

  override fun onBindViewHolder(holder: CellViewHolder<T>, position: Int) {
    val data = getItem(position)
    // 只有当数据不为 null 时才触发绑定。如果想允许传递 null，可由具体的 Cell 泛型决定
    data?.let { holder.bind(it) }
  }

  open fun getItem(position: Int): T? {
    return if (position < 0 || position >= mList.size) null else mList[position]
  }

  override fun getItemCount(): Int = mList.size

  override fun onViewRecycled(holder: CellViewHolder<T>) {
    super.onViewRecycled(holder)
    holder.unbind()
  }

  override fun onViewAttachedToWindow(holder: CellViewHolder<T>) {
    super.onViewAttachedToWindow(holder)
    holder.mCell.onViewAttached()
  }

  override fun onViewDetachedFromWindow(holder: CellViewHolder<T>) {
    super.onViewDetachedFromWindow(holder)
    holder.mCell.onViewDetached()
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    destroyCells()
  }

  private fun destroyCells() {
    mCells.forEach { cell ->
      cell.onDestroy()
    }
    mCells.clear()
  }
}
