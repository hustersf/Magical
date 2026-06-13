package com.sofar.core.media

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File

class MediaLauncherActivity : ComponentActivity() {

  companion object {
    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".core.media.fileprovider"
    private const val PREFIX_IMAGE_CACHE = "CAMERA_"
    private const val SUFFIX_IMAGE_JPG = ".jpg"
    private const val CLIP_DATA_LABEL_IMAGES = "Selected Images"
    const val EXTRA_SELECTED_URIS = "EXTRA_SELECTED_URIS"
  }

  private var cameraImageUri: Uri? = null
  private var photoFile: File? = null

  // 1. 系统相册选择器（免权限）
  private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      finishWithListResult(uris)
    } else {
      finishWithCancel()
    }
  }

  // 2. 系统相机拍摄器（免权限）
  private val takePhotoLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicture()
  ) { isSuccess: Boolean ->
    val uri = cameraImageUri
    if (isSuccess && uri != null) {
      finishWithResult(uri)
    } else {
      deleteTempFile()
      finishWithCancel()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val actionStr =
      intent.getStringExtra(GetMediaContract.EXTRA_MEDIA_ACTION) ?: return finishWithCancel()
    when (MediaAction.valueOf(actionStr)) {
      MediaAction.PICK_IMAGE -> {
        pickImageLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      }

      MediaAction.TAKE_PHOTO -> {
        cameraImageUri = createCacheImageUri()
        cameraImageUri?.let { takePhotoLauncher.launch(it) } ?: finishWithCancel()
      }
    }
  }

  // 在私有缓存目录（cacheDir）生成安全的 FileProvider 门牌号
  private fun createCacheImageUri(): Uri? {
    return try {
      val file = File.createTempFile(PREFIX_IMAGE_CACHE, SUFFIX_IMAGE_JPG, cacheDir)
      photoFile = file
      val authority = "${this.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX"
      FileProvider.getUriForFile(this, authority, file)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun deleteTempFile() {
    try {
      photoFile?.let { file ->
        if (file.exists()) file.delete()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      photoFile = null
    }
  }

  private fun finishWithResult(uri: Uri) {
    val intent = Intent().apply { data = uri }
    setResult(RESULT_OK, intent)
    finish()
  }

  private fun finishWithListResult(uris: List<Uri>) {
    val intent = Intent().apply {
      // 将所有 Uri 放入 ClipData，确保跨页面时自动携带临时读取权限
      val clipData = ClipData.newUri(contentResolver, CLIP_DATA_LABEL_IMAGES, uris.first())
      for (i in 1 until uris.size) {
        clipData.addItem(ClipData.Item(uris[i]))
      }
      this.clipData = clipData
      // 放入 ArrayList 方便上层业务直接获取列表
      putParcelableArrayListExtra(EXTRA_SELECTED_URIS, ArrayList(uris))
    }
    setResult(RESULT_OK, intent)
    finish()
  }

  private fun finishWithCancel() {
    setResult(RESULT_CANCELED)
    finish()
  }
}