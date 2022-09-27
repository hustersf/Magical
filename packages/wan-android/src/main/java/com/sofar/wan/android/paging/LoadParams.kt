package com.sofar.wan.android.paging

class LoadParams<Key : Any>(
  val loadType: LoadType,
  val prevKey: Key?,
  val nextKey: Key?,
)