package com.sofar.feature.ai.edge.models.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelAllowlist
import com.sofar.core.ui.UiState
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import kotlinx.coroutines.launch

class ModelsHomeFragment : Fragment() {

  private lateinit var modelsRv: RecyclerView
  private lateinit var adapter: ModelsAdapter

  private val viewModel: ModelsManagerViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.models_home_fragment, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView(view)
    initData()
  }

  private fun initView(view: View) {
    modelsRv = view.findViewById(R.id.models_rv)
    modelsRv.layoutManager = LinearLayoutManager(context)
    val padding = resources.getDimension(R.dimen.core_ui_spacing_lg).toInt()
    modelsRv.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.VERTICAL,
        0, padding, padding
      )
    )
    adapter = ModelsAdapter()
    modelsRv.adapter = adapter
  }

  private fun initData() {
    viewModel.fetchConfig()
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
          when (state) {
            is UiState.Loading -> {
              // 展示加载中
            }

            is UiState.Success -> {
              // 拿到数据，渲染 UI
              updateUI(state.data)
            }

            is UiState.Error -> {
              // 展示错误提示
            }
          }
        }
      }
    }
  }

  private fun updateUI(data: ModelAllowlist) {
    val list = mutableListOf<Model>()
    for (item in data.models) {
      list.add(item.toModel())
    }
    adapter.submitList(list)
  }
}
