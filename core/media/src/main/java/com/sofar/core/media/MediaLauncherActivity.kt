package com.sofar.core.media

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

class MediaLauncherActivity : ComponentActivity() {

  companion object {
    // FileProvider 授权后缀名
    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".core.media.fileprovider"

    // 💡 统一规范的物理文件管理常量
    private const val DIR_MEDIA_IMAGES = "media_images"
    private const val PREFIX_IMAGE = "IMG_"
    private const val SUFFIX_IMAGE_JPG = "jpg"
    private const val DEFAULT_IMAGE_EXTENSION = "jpg"

    // 流拷贝缓冲区大小 (4KB)
    private const val BUFFER_SIZE = 4096

    // 👈 统一对外的传参及返回 Key，彻底消灭魔法字符串
    const val EXTRA_SELECTED_PATHS = "EXTRA_SELECTED_PATHS"
  }

  private var cameraImageUri: Uri? = null
  private var photoFile: File? = null

  // 系统相册选择器（免权限）
  private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      // 利用协程执行独立的流拷贝职责，完成后再调用导航返回，职责彻底分离
      lifecycleScope.launch {
        val savedPaths = copyUrisToInternalStorage(uris)
        if (savedPaths.isNotEmpty()) {
          finishWithPathsResult(savedPaths)
        } else {
          finishWithCancel()
        }
      }
    } else {
      finishWithCancel()
    }
  }

  // 2. 系统相机拍摄器（免权限）
  private val takePhotoLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicture()
  ) { isSuccess: Boolean ->
    val uri = cameraImageUri
    val file = photoFile
    if (isSuccess && uri != null && file != null) {
      // 相机和相册现在共享相同的物理存储目录与命名规范，直接返回路径列表，0二次拷贝
      finishWithPathsResult(arrayListOf(file.absolutePath))
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
        cameraImageUri = createCameraImageUri()
        cameraImageUri?.let { takePhotoLauncher.launch(it) } ?: finishWithCancel()
      }
    }
  }

  /**
   * 统一路径与文件命名规范的内部工厂函数
   * 支持由外部传入特定的 fileName。若未传，则默认自动生成随机 UUID。
   * 优先生成外部缓存路径规范：Android/data/包名/cache/media_images/IMG_自定义名称或UUID.extension
   */
  private fun createMediaFile(
    extension: String,
    fileName: String = UUID.randomUUID().toString()
  ): File {
    val baseDir = externalCacheDir ?: cacheDir

    val mediaImageDir = File(baseDir, DIR_MEDIA_IMAGES).apply {
      if (!exists()) mkdirs()
    }
    return File(mediaImageDir, "$PREFIX_IMAGE$fileName.$extension")
  }

  private fun md5(input: String): String {
    return try {
      val digest = MessageDigest.getInstance("MD5")
      digest.update(input.toByteArray())
      val messageDigest = digest.digest()
      val hexString = StringBuilder()
      for (aMessageDigest in messageDigest) {
        var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
        while (h.length < 2) h = "0$h"
        hexString.append(h)
      }
      hexString.toString() // 返回一个 32 位的十六进制纯文本安全安全文件名
    } catch (e: Exception) {
      // 如果极罕见情况下初始化失败，用 UUID 降级兜底，确保程序不崩溃
      UUID.randomUUID().toString()
    }
  }

  /**
   * 将相册临时 Uri 统一安全地拷贝为永久有效的内部物理路径，彻底根治跨进程权限失效导致的读取异常。
   */
  private suspend fun copyUrisToInternalStorage(uris: List<Uri>): ArrayList<String> {
    return withContext(Dispatchers.IO) {
      val localPaths = arrayListOf<String>()

      for (sourceUri in uris) {
        try {
          // 智能解析相册图片的真实后缀格式（如 png, webp 等）
          val mimeType = contentResolver.getType(sourceUri)
          val fileExtension =
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
              ?: DEFAULT_IMAGE_EXTENSION
          val uniqueFileName = md5(sourceUri.toString())

          // 调用统一文件生成规则
          val targetFile = createMediaFile(fileExtension, uniqueFileName)

          if (targetFile.exists() && targetFile.length() > 0) {
            localPaths.add(targetFile.absolutePath)
            continue
          }

          // 💡 利用 Kotlin .use 语法糖，在执行完毕或中途发生异常时自动闭环安全关闭输入/输出流
          contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
              val buffer = ByteArray(BUFFER_SIZE)
              var bytesRead: Int
              while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
              }
              outputStream.flush()

              // 拷贝成功后记录物理路径
              localPaths.add(targetFile.absolutePath)
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      return@withContext localPaths
    }
  }

  // 配合统一规范生成的 FileProvider 门牌号
  private fun createCameraImageUri(): Uri? {
    return try {
      // 统一调用规范函数，确保相机拍出来的照片也处于同一个口袋、相同的命名格式下
      val file = createMediaFile(SUFFIX_IMAGE_JPG)
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

  /**
   * 🛠️ 纯粹的导航返回函数：数据完全准备就绪后的规范出口
   */
  private fun finishWithPathsResult(paths: ArrayList<String>) {
    val intent = Intent().apply {
      putStringArrayListExtra(EXTRA_SELECTED_PATHS, paths)
    }
    setResult(RESULT_OK, intent)
    finish()
  }

  private fun finishWithCancel() {
    setResult(RESULT_CANCELED)
    finish()
  }
}
