package com.sofar.core.ui.recyclerview.adapter.multitype

fun interface Linker<T> {

  fun index(position: Int, data: T): Int
}
