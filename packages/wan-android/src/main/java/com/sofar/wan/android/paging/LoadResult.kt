package com.sofar.wan.android.paging

sealed class LoadResult<Key : Any, Value : Any> {

  class Error<Key : Any, Value : Any>(
    val throwable: Throwable,
  ) : LoadResult<Key, Value>()

  class Page<Key : Any, Value : Any> constructor(
    /**
     * Loaded data
     */
    val data: List<Value>,
    /**
     * [Key] for previous page if more data can be loaded in that direction, `null`
     * otherwise.
     */
    val prevKey: Key?,
    /**
     * [Key] for next page if more data can be loaded in that direction, `null` otherwise.
     */
    val nextKey: Key?,
  ) : LoadResult<Key, Value>()
}
