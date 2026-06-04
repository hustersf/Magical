package com.sofar.feature.ai.edge.models.impl

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.repository.ModelsDataManager
import com.sofar.core.ai.edge.data.storage.AppStorageHub
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration
import com.sofar.core.ui.state.observeEvent
import kotlinx.coroutines.launch

class ModelsHomeFragment : Fragment() {

  private lateinit var storageUsageTv: TextView
  private lateinit var storagePercentTv: TextView
  private lateinit var storageCircularProgress: CircularProgressIndicator
  private lateinit var modelsRv: RecyclerView
  private lateinit var adapter: ModelsAdapter

  private val viewModel: ModelsManagerViewModel by viewModels()

  private var lastStorageRefreshTime: Long = 0L

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.feature_models_home_fragment, container, false);
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView(view)
    initData()
  }

  private fun initView(view: View) {
    storageUsageTv = view.findViewById(R.id.storage_usage_tv)
    storagePercentTv = view.findViewById(R.id.storage_progress_tv)
    storageCircularProgress = view.findViewById(R.id.storage_progress)
    modelsRv = view.findViewById(R.id.models_rv)
    modelsRv.layoutManager = LinearLayoutManager(context)
    val padding = resources.getDimension(R.dimen.core_ui_spacing_lg).toInt()
    modelsRv.addItemDecoration(
      LinearMarginItemDecoration(
        RecyclerView.VERTICAL,
        0, padding, padding
      )
    )
    adapter = ModelsAdapter(
      onActionClick = { model, view ->
        when (view.id) {
          R.id.download_btn -> viewModel.downloadModel(model)
          R.id.model_delete_iv -> showDeleteConfirmationDialog(model)
          R.id.benchmark_btn -> jump(model)
          R.id.try_it_btn -> jump(model)
        }
      }
    )
    modelsRv.adapter = adapter
  }

  private fun initData() {
    viewModel.fetchConfig()
    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state ->
          updateUI(state)
          state.errorMessage.observeEvent {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }
  }

  private fun updateUI(state: ModelManagerUiState) {
    val uiModels = state.models.map { model ->
      ModelUiState(
        model = model,
        status = state.modelDownloadStatus[model.name]
      )
    }
    adapter.submitList(uiModels)

    val currentTime = System.currentTimeMillis()
    if (state.isScanningStorage || (currentTime - lastStorageRefreshTime >= 1000L)) {
      renderStorageCard(state) // 放行刷新卡片并读取物理大盘
      lastStorageRefreshTime = currentTime // 🌟 重新咬死时间戳起点
    }
  }

  private fun renderStorageCard(state: ModelManagerUiState) {
    val context = storageUsageTv.context
    val snapshot = AppStorageHub.getStorageSnapshot(context)
    val usedStr = Formatter.formatFileSize(context, snapshot.modelsSize)
    val availStr = Formatter.formatFileSize(context, snapshot.availableSize)
    storageUsageTv.text = getString(R.string.feature_models_storage_used_format, usedStr, availStr)

    val totalSpaceDenom = snapshot.modelsSize + snapshot.availableSize
    val progressPercent = if (totalSpaceDenom > 0) {
      (snapshot.modelsSize.toFloat() / totalSpaceDenom * 100).toInt()
    } else {
      0
    }
    storageCircularProgress.progress = progressPercent
    storagePercentTv.text = "$progressPercent%"
  }

  private fun showDeleteConfirmationDialog(model: Model) {
    MaterialAlertDialogBuilder(requireContext())
      .setTitle(getString(R.string.feature_models_delete_dialog_title))
      .setMessage(getString(R.string.feature_models_delete_dialog_message, model.name))
      .setCancelable(true)
      .setPositiveButton(getString(R.string.feature_models_delete_dialog_confirm)) { dialog, _ ->
        viewModel.deleteModel(model)
        dialog.dismiss()
      }
      .setNegativeButton(getString(R.string.feature_models_delete_dialog_cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .show()
  }

  private fun jump(model: Model) {
    ModelsDataManager.updateActiveModel(model)
  }
}
