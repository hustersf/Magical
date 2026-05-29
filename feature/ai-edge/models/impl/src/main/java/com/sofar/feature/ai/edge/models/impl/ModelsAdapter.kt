package com.sofar.feature.ai.edge.models.impl

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sofar.core.ai.edge.data.entity.models.Model
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatus
import com.sofar.core.ai.edge.data.entity.models.ModelDownloadStatusType
import io.noties.markwon.Markwon

class ModelsAdapter(
  private val onActionClick: (Model, View) -> Unit,
  private val diffCallback: ModelDiffCallback = ModelDiffCallback()
) : ListAdapter<ModelUiState, ModelViewHolder>(diffCallback) {

  companion object {
    const val PAYLOAD_PROGRESS_ONLY = "payload_progress_only"
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.feature_models_adapter_item, parent, false)
    return ModelViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item.model, item.status, onActionClick)
  }

  override fun onBindViewHolder(holder: ModelViewHolder, position: Int, payloads: List<Any?>) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads)
      return
    }
    val bundle = payloads.firstOrNull() as? Bundle
    val isProgressUpdate = bundle?.containsKey(PAYLOAD_PROGRESS_ONLY) == true
    if (isProgressUpdate) {
      val item = getItem(position)
      holder.updateStatusUiOnly(item.model, item.status)
    } else {
      super.onBindViewHolder(holder, position, payloads)
    }
  }
}

class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val nameTv: TextView = itemView.findViewById(R.id.model_name_tv)
  private val sizeTv: TextView = itemView.findViewById(R.id.model_size_tv)
  private val descTv: TextView = itemView.findViewById(R.id.model_description_tv)
  private val deleteIv: ImageView = itemView.findViewById(R.id.model_delete_iv)
  private val downloadBtn: Button = itemView.findViewById(R.id.download_btn)
  private val benchmarkBtn: Button = itemView.findViewById(R.id.benchmark_btn)
  private val tryBtn: Button = itemView.findViewById(R.id.try_it_btn)

  private val markwon = Markwon.create(descTv.context)
  fun bind(item: Model, status: ModelDownloadStatus?, onActionClick: (Model, View) -> Unit) {
    nameTv.text = item.name
    sizeTv.text = Formatter.formatFileSize(sizeTv.context, item.sizeInBytes)

    markwon.setMarkdown(descTv, item.info)

    downloadBtn.setOnClickListener { onActionClick(item, it) }
    benchmarkBtn.setOnClickListener { onActionClick(item, it) }
    tryBtn.setOnClickListener { onActionClick(item, it) }
    deleteIv.setOnClickListener { onActionClick(item, it) }

    updateStatusUiOnly(item, status)
  }

  fun updateStatusUiOnly(item: Model, status: ModelDownloadStatus?) {
    downloadBtn.visibility = View.GONE
    benchmarkBtn.visibility = View.GONE
    tryBtn.visibility = View.GONE
    deleteIv.visibility = View.GONE

    //  如果 status 为 null，直接判定为未下载状态，并提前结束
    val context = downloadBtn.context
    if (status == null) {
      downloadBtn.visibility = View.VISIBLE
      downloadBtn.text = context.getString(R.string.feature_models_download)
      downloadBtn.isEnabled = true
      return
    }

    val progressInt = if (status.totalBytes > 0L) {
      ((status.receivedBytes * 100) / status.totalBytes).toInt()
    } else {
      0
    }

    when (status.statusType) {
      ModelDownloadStatusType.NOT_DOWNLOADED -> {
        downloadBtn.visibility = View.VISIBLE
        downloadBtn.text = context.getString(R.string.feature_models_download)
        downloadBtn.isEnabled = true
      }

      ModelDownloadStatusType.PARTIALLY_DOWNLOADED -> {
        downloadBtn.visibility = View.VISIBLE
        downloadBtn.text =
          context.getString(R.string.feature_models_download_partial, progressInt)
        downloadBtn.isEnabled = true
      }

      ModelDownloadStatusType.IN_PROGRESS -> {
        downloadBtn.visibility = View.VISIBLE

        val currentStr = Formatter.formatFileSize(context, status.receivedBytes)
        val totalStr = Formatter.formatFileSize(context, status.totalBytes)
        val remainingSeconds = if (status.bytesPerSecond > 0L) {
          (status.totalBytes - status.receivedBytes) / status.bytesPerSecond
        } else {
          0L
        }
        val minutes = (remainingSeconds / 60).coerceAtLeast(1)
        val remainingTimeStr =
          context.getString(R.string.feature_models_time_minutes, minutes)
        downloadBtn.text = context.getString(
          R.string.feature_models_download_progress,
          progressInt, currentStr, totalStr, remainingTimeStr
        )
        downloadBtn.isEnabled = false // 下载中途拦截用户的二次重复点击
      }

      ModelDownloadStatusType.UNZIPPING -> {
        downloadBtn.visibility = View.VISIBLE
        downloadBtn.text = context.getString(R.string.feature_models_download_unzipping)
        downloadBtn.isEnabled = false
      }

      ModelDownloadStatusType.SUCCEEDED -> {
        if (item.showBenchmarkButton) benchmarkBtn.visibility = View.VISIBLE
        if (item.showRunAgainButton) tryBtn.visibility = View.VISIBLE
        deleteIv.visibility = View.VISIBLE
      }

      ModelDownloadStatusType.FAILED -> {
        downloadBtn.visibility = View.VISIBLE
        downloadBtn.text = context.getString(R.string.feature_models_download_retry)
        downloadBtn.isEnabled = true
      }
    }
  }
}