package com.sofar.snapu.feature.daq.product

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.player.VideoFrameExtractor
import com.sofar.snapu.MainActivity
import com.sofar.snapu.R
import com.sofar.snapu.feature.daq.BitmapUtil
import com.sofar.snapu.feature.daq.TaskManager
import com.sofar.snapu.feature.daq.TaskUtil
import java.io.File

class CaptureVideoFrameActivity : BaseUIActivity() {

  private lateinit var photoIv: ImageView
  private lateinit var saveBtn: Button
  private lateinit var confirmBtn: Button

  private var taskId: Long = 0
  private lateinit var videoFile: File
  private lateinit var videoFrameFile: File

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    iniData()
  }

  override fun onCreateView(
    layoutInflater: LayoutInflater,
    viewGroup: ViewGroup?,
    bundle: Bundle?
  ): View {
    val view: View = layoutInflater.inflate(R.layout.capture_video_frame_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    photoIv = findViewById(R.id.photo_iv)
    saveBtn = findViewById(R.id.save_btn)
    saveBtn.setOnSingleClickListener({
      TaskManager.get().productSave()
      launchMain()
    })
    confirmBtn = findViewById(R.id.confirm_btn)
    confirmBtn.setOnSingleClickListener({
      TaskManager.get().productSubmit()
      launchMain()
    })

    setTitle(ContextCompat.getString(this, R.string.product_title))
    setNavigationIcon(R.drawable.nav_back, {
      finish()
    })
  }

  private fun iniData() {
    taskId = intent.getLongExtra(TaskUtil.KET_TASK_ID, 0)
    videoFile = TaskUtil.getProductVideoFile(this, taskId)
    videoFrameFile = TaskUtil.getProductVideoFrameFile(this, taskId)
    VideoFrameExtractor.extractFrameAsync(
      videoFile.absolutePath,
      FRAME_TIME,
      object : VideoFrameExtractor
      .OnFrameExtractedListener {
        override fun onFrameExtracted(bitmap: Bitmap?) {
          bitmap?.let {
            BitmapUtil.saveToFile(it, videoFrameFile)
          }
          runOnUiThread({
            photoIv.setImageBitmap(bitmap)
            TaskManager.get().productVideoFrame(videoFrameFile.absolutePath)
          })
        }
      })
  }


  override fun onDestroy() {
    super.onDestroy()
  }

  private fun launchMain() {
    MainActivity.launch(this)
  }


  companion object {

    const val FRAME_TIME = 0L

    fun launch(context: Context, taskId: Long) {
      val intent = Intent(context, CaptureVideoFrameActivity::class.java)
      intent.putExtra(TaskUtil.KET_TASK_ID, taskId)
      context.startActivity(intent)
    }
  }
}