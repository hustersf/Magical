package com.sofar.widget.recycler.adapter.multitype;

import java.util.List;

import androidx.annotation.NonNull;

import com.sofar.widget.recycler.adapter.Cell;
import com.sofar.widget.recycler.adapter.CellAdapter;

/**
 * 支持不同数据类型的列表
 */
public class MultiTypeAdapter extends CellAdapter {

  private MultiTypes mTypes = new MultiTypes();

  /**
   * 一对一关系
   *
   * @param clazz 数据类型
   * @param cell  一种数据类型对应一种cell
   */
  public <T> void register(Class<T> clazz, Cell<T> cell) {
    mTypes.register(new Type(clazz, cell, new DefaultLinker()));
  }

  /**
   * 一对多关系
   *
   * @param clazz  数据类型
   * @param cells  一种数据类型对应多种cell
   * @param linker 根据数据从 cells 中找出相应的cell
   */
  public <T> void register(Class<T> clazz, List<Cell<T>> cells, Linker<T> linker) {
    for (Cell cell : cells) {
      mTypes.register(new Type(clazz, cell, linker));
    }
  }

  @NonNull
  @Override
  protected Cell onCreateCell(int viewType) {
    return mTypes.getType(viewType).mCell;
  }

  @Override
  public int getItemViewType(int position) {
    return indexOfType(position);
  }

  private int indexOfType(int position) {
    Object item = getItem(position);
    int index = mTypes.firstIndex(item.getClass());
    if (index != -1) {
      Linker linker = mTypes.getType(index).mLinker;
      return index + linker.index(position, item);
    }
    throw new IllegalStateException("you must register class=" + item.getClass());
  }
}
