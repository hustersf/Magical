package com.sofar.wan.android.paging

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AsyncPageDataDiffer<T : Any>(
  private val adapter: RecyclerView.Adapter<*>,
  private val diffCallback: DiffUtil.ItemCallback<T>,
  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
  private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

  private var list = emptyList<T>()

  suspend fun submitData(newList: List<T>) {
    var result = withContext(workerDispatcher) {
      DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
          return list.size
        }

        override fun getNewListSize(): Int {
          return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
          return diffCallback.areItemsTheSame(list[oldItemPosition], newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
          return diffCallback.areContentsTheSame(list[oldItemPosition], newList[newItemPosition])
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
          return diffCallback.getChangePayload(list[oldItemPosition], newList[newItemPosition])
        }
      })
    }

    withContext(mainDispatcher) {
      list = newList
      result.dispatchUpdatesTo(adapter)
    }
  }

}