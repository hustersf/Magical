package com.sofar.wan.android.paging

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class PageLoadEventDetector(
  private val fragment: PageFragment<*>,
  private val pageList: PageList<*, *>,
) :
  RecyclerView.OnScrollListener() {

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    super.onScrolled(recyclerView, dx, dy)
    val change = dx != 0 || dy != 0
    if (change) {
      fragment.lifecycleScope.launch {
        tryToLoad(recyclerView)
      }
    }
  }

  private suspend fun tryToLoad(recyclerView: RecyclerView) {
    val manager = recyclerView.layoutManager ?: return
    if (manager.childCount > 0 && !pageList.isEmpty()) {
      val count = manager.itemCount
      //前后加载
      val childView = manager.getChildAt(manager.childCount - 1) ?: return
      val last = recyclerView.getChildAdapterPosition(childView)
      if (last == count - 1 && pageList.hasNext()) {
        pageList.load(LoadType.APPEND)
        return
      }

      //向前加载
      val firstView = manager.getChildAt(manager.childCount - 1) ?: return
      val first = recyclerView.getChildAdapterPosition(firstView)
      if (first == 0 && pageList.hasPrev()) {
        pageList.load(LoadType.PREPEND)
        return
      }
    }
  }

}