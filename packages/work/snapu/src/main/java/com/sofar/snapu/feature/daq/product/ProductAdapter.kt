package com.sofar.snapu.feature.daq.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sofar.auto.play.Playable
import com.sofar.base.rx.RxBus
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.player.VideoPlayer
import com.sofar.snapu.R
import com.sofar.snapu.feature.daq.TaskUtil
import com.sofar.snapu.feature.daq.model.Product
import com.sofar.utility.ViewUtil
import com.sofar.widget.recycler.adapter.Cell
import com.sofar.widget.recycler.adapter.CellAdapter

class ProductAdapter : CellAdapter<Product>() {
  override fun onCreateCell(viewType: Int): Cell<Product> {
    return ProductCell()
  }

  class ProductCell : Cell<Product>(), Playable {

    private lateinit var eanTv: TextView
    private lateinit var nameTv: TextView
    private lateinit var timeTv: TextView
    private lateinit var eanIv: ImageView
    private lateinit var frameIv: ImageView

    private lateinit var editBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var uploadBtn: Button

    private lateinit var playerView: PlayerView
    private lateinit var player: VideoPlayer

    private var product: Product? = null

    override fun createView(parent: ViewGroup): View {
      return LayoutInflater.from(parent.context).inflate(R.layout.product_list_item, parent, false)
    }

    override fun onCreate(rootView: View) {
      super.onCreate(rootView)
      eanTv = rootView.findViewById(R.id.ean_tv)
      nameTv = rootView.findViewById(R.id.name_tv)
      timeTv = rootView.findViewById(R.id.time_tv)
      eanIv = rootView.findViewById(R.id.ean_iv)
      frameIv = rootView.findViewById(R.id.frame_iv)

      editBtn = rootView.findViewById(R.id.edit_btn)
      editBtn.setOnSingleClickListener {
        product?.let {
          CaptureProductActivity.launch(rootView.context, it.taskId)
        }
      }
      deleteBtn = rootView.findViewById(R.id.delete_btn)
      deleteBtn.setOnSingleClickListener {
        showDialog(rootView.context, R.id.delete_btn, R.string.dialog_title_confirm_delete)
      }
      uploadBtn = rootView.findViewById(R.id.upload_btn)
      uploadBtn.setOnSingleClickListener {
        showDialog(rootView.context, R.id.upload_btn, R.string.dialog_title_confirm_upload)
      }

      playerView = rootView.findViewById(R.id.player_view)
      player = VideoPlayer(rootView.context)
      player.setPlayerView(playerView)
    }

    override fun onBind(data: Product) {
      super.onBind(data)
      this.product = data

      eanTv.text = data.ean
      nameTv.text = data.name
      timeTv.text = TaskUtil.formatDateInSecond(data.taskId)

      Glide.with(eanIv.context)
        .load(data.imageFile)
        .into(eanIv)
      Glide.with(frameIv.context)
        .load(data.videoFrameFile)
        .into(frameIv)
      player.setDataSource(data.videoFile)

      updateUI(data)
    }

    private fun updateUI(data: Product) {
      uploadBtn.isEnabled = !data.uploaded
      editBtn.visibility = if (data.uploaded) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
      super.onDestroy()
      player.release()
    }

    override fun start() {
      player.start()
    }

    override fun stop() {
      player.stop()
    }

    override fun getViewShowRatio(): Float {
      return ViewUtil.getViewShowRatio(playerView)
    }

    private fun showDialog(context: Context, id: Int, titleResId: Int) {
      MaterialAlertDialogBuilder(context)
        .setTitle(titleResId)
        .setPositiveButton(R.string.action_confirm) { dialog, which ->
          confirm(context, id)
        }
        .setNegativeButton(R.string.action_cancel, null)
        .show()
    }

    private fun confirm(context: Context, id: Int) {
      when (id) {
        R.id.upload_btn -> {
          product?.let {
            it.uploaded = true
            TaskUtil.writeProductAsync(context, it) {
              uploadBtn.post {
                updateUI(it)
              }
            }
          }
        }

        R.id.delete_btn -> {
          product?.let {
            RxBus.get().post(ProductEvent.ListDeleteEvent(it))
          }
        }
      }
    }
  }

}