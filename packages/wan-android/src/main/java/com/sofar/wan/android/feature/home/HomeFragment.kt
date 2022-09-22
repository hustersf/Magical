package com.sofar.wan.android.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sofar.base.BaseFragment
import com.sofar.wan.android.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

  private lateinit var recyclerView: RecyclerView

  private val viewModel: HomeViewModel by lazy {
    ViewModelProvider(this).get(HomeViewModel::class.java)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.home_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(context)
    initAdapter()
  }

  private fun initAdapter() {
    var adapter = HomeAdapter()
    recyclerView.adapter = adapter

    lifecycleScope.launch {
      viewModel.pageFlow.collectLatest {
        adapter.submitData(it)
      }
    }
  }

}