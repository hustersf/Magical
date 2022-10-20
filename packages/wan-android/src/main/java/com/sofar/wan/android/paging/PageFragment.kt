package com.sofar.wan.android.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sofar.base.BaseFragment
import com.sofar.wan.android.R
import com.sofar.widget.recycler.adapter.CellAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class PageFragment<MODEL : Any> : BaseFragment() {

  private var refreshLayout: SwipeRefreshLayout? = null
  protected lateinit var recyclerView: RecyclerView

  private lateinit var adapter: CellAdapter<MODEL>
  private lateinit var pageList: PageList<*, MODEL>
  private lateinit var autoLoadEventDetector: RecyclerView.OnScrollListener
  private lateinit var differ: AsyncPageDataDiffer<MODEL>

  open fun getLayoutResId(): Int {
    return R.layout.base_page_fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    var rootView: View = inflater.inflate(getLayoutResId(), container, false)
    refreshLayout = rootView.findViewById(R.id.refresh_layout)
    recyclerView = rootView.findViewById(R.id.recycler_view)
    return rootView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initRefreshLayout()
    initRecyclerView()

    pageList = onCreatePageList()
    autoLoadEventDetector = onCreateAutoLoadEventDetector()
    recyclerView.addOnScrollListener(autoLoadEventDetector)
    observePage()
    if (autoLoad()) {
      refresh()
    }
  }

  private fun initRefreshLayout() {
    refreshLayout?.setOnRefreshListener {
      refresh()
    }
  }

  private fun initRecyclerView() {
    recyclerView.layoutManager = onCreateLayoutManager()
    adapter = onCreateAdapter()
    recyclerView.adapter = adapter
    differ = AsyncPageDataDiffer(adapter, onCreateDiffCallback())
  }

  private fun observePage() {
    lifecycleScope.launch {
      pageList.dataFlow.collect {
        adapter.items = it
        differ.submitData(it)
      }
    }

    lifecycleScope.launch {
      pageList.loadStateFlow
        .map { it.refresh }
        .distinctUntilChanged()
        .collect {
          if (it !is LoadState.Loading) {
            refreshLayout?.isRefreshing = false
          }
        }
    }
  }

  fun pageList(): PageList<*, MODEL> {
    return pageList
  }

  open fun onCreateAutoLoadEventDetector(): RecyclerView.OnScrollListener {
    return PageLoadEventDetector(this, pageList)
  }

  open fun onCreateLayoutManager(): RecyclerView.LayoutManager {
    return LinearLayoutManager(context)
  }

  protected abstract fun onCreateAdapter(): CellAdapter<MODEL>

  protected abstract fun onCreatePageList(): PageList<*, MODEL>

  protected abstract fun onCreateDiffCallback(): DiffUtil.ItemCallback<MODEL>

  open fun autoLoad(): Boolean {
    return true
  }

  fun refresh() {
    lifecycleScope.launch {
      pageList.load(LoadType.REFRESH)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    recyclerView.removeOnScrollListener(autoLoadEventDetector)
    recyclerView.adapter = null
  }
}