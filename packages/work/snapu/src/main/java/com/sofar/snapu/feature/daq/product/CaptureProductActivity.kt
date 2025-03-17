package com.sofar.snapu.feature.daq.product

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.sofar.base.app.BaseUIActivity
import com.sofar.base.util.setOnSingleClickListener
import com.sofar.snapu.R
import android.util.Pair
import com.sofar.mlkit.barcode.BarcodeResult
import com.sofar.mlkit.barcode.BarcodeScannerProcessor
import com.sofar.mlkit.core.GraphicOverlay
import com.sofar.mlkit.core.VisionImageProcessor
import com.sofar.snapu.feature.daq.BitmapUtil
import com.sofar.snapu.feature.daq.CaptureFileUtil
import com.sofar.snapu.feature.daq.TaskManager
import com.sofar.snapu.feature.daq.TaskUtil
import com.sofar.utility.DeviceUtil
import java.io.File

class CaptureProductActivity : BaseUIActivity() {

  private lateinit var photoIv: ImageView
  private lateinit var graphicOverlay: GraphicOverlay
  private lateinit var eanEt: EditText
  private lateinit var nameEt: EditText
  private lateinit var captureBtn: Button
  private lateinit var confirmBtn: Button

  private lateinit var imageFile: File
  private lateinit var imageUri: Uri
  private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

  private var imageMaxWith = 0
  private var imageMaxHeight = 0

  private var taskId: Long = 0
  private var imageProcessor: VisionImageProcessor? = null

  private var eanTextWatcher = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
      confirmBtn.isEnabled = !TextUtils.isEmpty(s)
    }

    override fun afterTextChanged(s: Editable?) {

    }

  }

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
    val view: View = layoutInflater.inflate(R.layout.capture_product_activity, viewGroup, false)
    return view
  }

  private fun initView() {
    photoIv = findViewById(R.id.photo_iv)
    graphicOverlay = findViewById(R.id.graphic_overlay)
    eanEt = findViewById(R.id.ean_edittext)
    eanEt.addTextChangedListener(eanTextWatcher)
    nameEt = findViewById(R.id.name_edittext)
    captureBtn = findViewById(R.id.capture_btn)
    captureBtn.setOnSingleClickListener({
      captureImage()
    })
    confirmBtn = findViewById(R.id.confirm_btn)
    confirmBtn.setOnSingleClickListener({
      val ean = eanEt.text.toString().trim()
      val name = nameEt.text.toString().trim()
      TaskManager.get().productEAN(ean, name, imageFile.absolutePath)
      CaptureVideoActivity.launch(this, taskId)
    })

    setTitle(ContextCompat.getString(this, R.string.product_title))
    setNavigationIcon(R.drawable.nav_back, {
      finish()
    })
  }

  private fun iniData() {
    imageProcessor = BarcodeScannerProcessor(this, object : BarcodeResult {
      override fun success(result: String) {
        eanEt.setText(result)
      }

      override fun error(e: Exception) {
      }
    })
    imageMaxWith = DeviceUtil.getMetricsWidth(this)
    imageMaxHeight = DeviceUtil.getMetricsHeight(this)
    taskId = intent.getLongExtra(TaskUtil.KET_TASK_ID, 0)
    var edit = false
    if (taskId <= 0) {
      taskId = TaskUtil.buildTaskId()
    } else {
      edit = true
    }
    TaskManager.get().productStart(taskId, edit)
    imageFile = TaskUtil.getProductPhotoFile(this, taskId)
    imageUri = CaptureFileUtil.getUriForFile(this, imageFile)
    takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture())
    { result ->
      if (result) {
        takePictureSuccess()
      }
    }
    if (TaskManager.get().isEdit()) {
      takePictureSuccess()
    } else {
      captureImage()
    }
  }


  private fun captureImage() {
    takePictureLauncher.launch(imageUri)
  }

  private fun takePictureSuccess() {
    BitmapUtil.getBitmapFromFilePth(imageFile.path)?.let {
      val targetedSize: Pair<Int, Int> =
        if (photoIv.width != 0) Pair(photoIv.width, photoIv.height) else Pair(
          imageMaxWith,
          imageMaxHeight
        )
      graphicOverlay.clear()
      val resizedBitmap = BitmapUtil.resizeBitmap(targetedSize, it)
      photoIv.setImageDrawable(null)
      photoIv.setImageBitmap(resizedBitmap)
      eanEt.setText("")
      nameEt.setText("")

      graphicOverlay.setImageSourceInfo(
        resizedBitmap.width,
        resizedBitmap.height,
        false
      )
      imageProcessor?.processBitmap(resizedBitmap, graphicOverlay)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    eanEt.removeTextChangedListener(eanTextWatcher)
  }


  companion object {
    fun launch(context: Context, taskId: Long = 0) {
      val intent = Intent(context, CaptureProductActivity::class.java)
      intent.putExtra(TaskUtil.KET_TASK_ID, taskId)
      context.startActivity(intent)
    }
  }
}