package com.sofar.core.ui.recyclerview.adapter.multitype

/**
 * 存储数据模型、单元格工厂以及路由选择器的映射关系实体
 */
data class Type(
  val mClass: Class<*>,
  val mFactory: CellFactory<*>,
  val mLinker: Linker<*>
)
