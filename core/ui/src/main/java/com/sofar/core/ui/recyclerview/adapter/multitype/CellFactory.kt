package com.sofar.core.ui.recyclerview.adapter.multitype

import com.sofar.core.ui.recyclerview.adapter.Cell

fun interface CellFactory<T> {

  fun onCreateCell(): Cell<T>
}
