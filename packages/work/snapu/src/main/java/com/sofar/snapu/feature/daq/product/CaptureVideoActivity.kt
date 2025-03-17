package com.sofar.snapu.feature.daq.product

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.media3.ui.PlayerView
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.player.VideoPlayer
import com.sofar.snapu.R
import com.sofar.snapu.feature.daq.CaptureFileUtil
import com.sofar.snapu.feature.daq.CaptureVideoExtra
import com.sofar.snapu.feature.daq.TaskManager
import com.sofar.snapu.feature.daq.TaskUtil
import java.io.File

class CaptureVideoActivity : BaseUIActivity() {

  private lateinit var videoBtn: Button
  private lateinit var confirmBtn: Button
  private lateinit var playerView: PlayerView

  private lateinit var videoFile: File
  private lateinit var videoUri: Uri
  private lateinit var videoPlayer: VideoPlayer
  private lateinit var captureVideoLauncher: ActivityResultLauncher<Uri>

  private var taskId: Long = 0

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
    val view: View = layoutInflater.inflate(R.layout.capture_video_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    videoBtn = findViewById(R.id.video_btn)
    videoBtn.setOnSingleClickListener {
      captureVideo()
    }

    confirmBtn = findViewById(R.id.confirm_btn)
    confirmBtn.setOnSingleClickListener {
      TaskManager.get().productVideo(videoFile.absolutePath)
      CaptureVideoFrameActivity.launch(this, taskId)
    }
    confirmBtn.isEnabled = false

    setTitle(ContextCompat.getString(this, R.string.product_title))
    setNavigationIcon(R.drawable.nav_back, {
      finish()
    })

    playerView = findViewById(R.id.player_view)
  }

  private fun iniData() {
    taskId = intent.getLongExtra(TaskUtil.KET_TASK_ID, 0)
    videoFile = TaskUtil.getProductVideoFile(this, taskId)
    videoUri = CaptureFileUtil.getUriForFile(this, videoFile)
    videoPlayer = VideoPlayer(this)
    videoPlayer.setPlayerView(playerView)
    captureVideoLauncher =
      registerForActivityResult(CaptureVideoExtra(TaskUtil.VIDEO_DURATION_SECONDS))
      { result ->
        if (result) {
          captureVideoSuccess()
        }
      }
    if (TaskManager.get().isEdit()) {
      captureVideoSuccess()
    } else {
      captureVideo()
    }
  }

  private fun captureVideo() {
    captureVideoLauncher.launch(videoUri)
  }

  private fun captureVideoSuccess() {
    if (!videoFile.exists()) {
      return
    }
    confirmBtn.isEnabled = true
    videoPlayer.setDataSource(videoFile.absolutePath)
    videoPlayer.start()
  }

  override fun onResume() {
    super.onResume()
    videoPlayer.resume()
  }

  override fun onPause() {
    super.onPause()
    videoPlayer.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    videoPlayer.release()
  }

  companion object {
    fun launch(context: Context, taskId: Long) {
      val intent = Intent(context, CaptureVideoActivity::class.java)
      intent.putExtra(TaskUtil.KET_TASK_ID, taskId)
      context.startActivity(intent)
    }
  }
}