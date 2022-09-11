package com.sofar.widget.recycler.adapter.multitype;

import com.sofar.widget.recycler.adapter.Cell;

public class Type {

  public final Class mClass;
  public final Cell mCell;
  public final Linker mLinker;

  public Type(Class clazz, Cell cell, Linker linker) {
    mClass = clazz;
    mCell = cell;
    mLinker = linker;
  }
}
