package com.sofar.snapu.feature.daq.product

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.sofar.base.BaseFragment
import com.sofar.base.rx.RxBus
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.mlkit.barcode.BarcodeContract
import com.sofar.snapu.R
import com.sofar.snapu.feature.daq.TaskUtil
import com.sofar.snapu.feature.daq.model.DataState
import com.sofar.snapu.feature.daq.model.ListMode
import com.sofar.snapu.feature.daq.model.Product
import com.sofar.snapu.feature.daq.model.ProductListViewModel
import com.sofar.snapu.widget.page.SimplePageStateUI
import io.reactivex.disposables.CompositeDisposable

class ProductListFragment : BaseFragment() {

  private lateinit var refreshLayout: SwipeRefreshLayout
  private lateinit var pageStateUI: SimplePageStateUI
  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: ProductAdapter
  private lateinit var player: ProductListPlayer

  private lateinit var scanBtn: Button
  private lateinit var scanLauncher: ActivityResultLauncher<Unit>
  private lateinit var addBtn: Button
  private lateinit var filterBtn: Button

  private var listMode = ListMode.LIST_ALL

  private var disposes = CompositeDisposable()

  private val viewModel: ProductListViewModel by lazy {
    ViewModelProvider(this)[ProductListViewModel::class.java]
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.product_list_fragment, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    pageStateUI = SimplePageStateUI(view as ViewGroup)
    refreshLayout = view.findViewById(R.id.refresh_layout)
    refreshLayout.setOnRefreshListener {
      viewModel.fetchData()
    }

    recyclerView = view.findViewById(R.id.list_view)
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.addItemDecoration(object : ItemDecoration() {
      override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
      ) {
        val padding = resources.getDimensionPixelOffset(R.dimen.page_padding_left)
        outRect.left = padding
        outRect.right = padding
        outRect.top = padding
        val totalCount = adapter.itemCount
        val childPosition = parent.getChildAdapterPosition(view)
        outRect.bottom = if (childPosition == totalCount - 1) 6 * padding else 0
      }
    })
    adapter = ProductAdapter()
    recyclerView.adapter = adapter
    player = ProductListPlayer()
    player.attach(recyclerView)

    scanBtn = view.findViewById(R.id.scan_btn)
    scanBtn.setOnSingleClickListener {
      scanLauncher.launch(Unit)
    }
    addBtn = view.findViewById(R.id.add_btn)
    addBtn.setOnSingleClickListener {
      CaptureProductActivity.launch(requireContext())
    }
    filterBtn = view.findViewById(R.id.filter_btn)
    filterBtn.setOnSingleClickListener {
      showMenu(filterBtn, R.menu.product_list_filter_menu)
    }
    initData()
  }

  private fun initData() {
    viewModel.dataState.observe(viewLifecycleOwner) { state ->
      when (state) {
        is DataState.Loading -> showLoading()
        is DataState.Success -> showData(state.data)
        is DataState.Error -> showError(state.exception)
      }
    }
    viewModel.fetchData()
    scanLauncher = registerForActivityResult(BarcodeContract()) { result ->
      result?.let {
        Snackbar.make(scanBtn, result, Snackbar.LENGTH_SHORT).show()
      } ?: run {

      }
    }
    disposes.add(RxBus.get().toObservable(ProductEvent.ListDeleteEvent::class.java).subscribe {
      TaskUtil.deleteProductAsync(requireContext(), it.product) {
        recyclerView.post {
          viewModel.deleteProduct(it.product)
          val pos = adapter.items.indexOf(it.product)
          adapter.items.removeAt(pos)
          adapter.notifyItemRemoved(pos)
        }
      }
    })
  }

  private fun showLoading() {
    if (adapter.items.isEmpty()) {
      pageStateUI.showLoading()
    }
  }

  private fun showData(list: List<Product>) {
    refreshLayout.isRefreshing = false
    pageStateUI.hideStateView()
    adapter.items = list
    adapter.notifyDataSetChanged()
    if (list.isEmpty()) {
      pageStateUI.showEmpty()
    } else {
      recyclerView.post {
        player.recapture()
      }
    }
  }

  private fun showError(e: Exception) {
    refreshLayout.isRefreshing = false
    pageStateUI.hideStateView()
    pageStateUI.showError()
  }

  private fun showMenu(v: View, @MenuRes menuRes: Int) {
    val popup = PopupMenu(v.context, v)
    popup.menuInflater.inflate(menuRes, popup.menu)
    popup.setForceShowIcon(true)
    popup.menu.findItem(findIdByListMode())?.isChecked = true
    popup.setOnMenuItemClickListener { item: MenuItem ->
      item.isChecked = !item.isChecked
      when (item.itemId) {
        R.id.list_all -> {
          updateListMode(ListMode.LIST_ALL)
        }

        R.id.list_upload_pending -> {
          updateListMode(ListMode.LIST_UPLOAD_PENDING)
        }

        R.id.list_uploaded -> {
          updateListMode(ListMode.LIST_UPLOADED)
        }
      }
      true
    }
    popup.show()
  }

  private fun updateListMode(mode: Int) {
    listMode = mode
    viewModel.filterList(listMode)
  }

  private fun findIdByListMode(): Int {
    return when (listMode) {
      ListMode.LIST_UPLOAD_PENDING -> {
        R.id.list_upload_pending
      }

      ListMode.LIST_UPLOADED -> {
        R.id.list_uploaded
      }

      else -> {
        R.id.list_all
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    player.detach()
    disposes.clear()
  }

}