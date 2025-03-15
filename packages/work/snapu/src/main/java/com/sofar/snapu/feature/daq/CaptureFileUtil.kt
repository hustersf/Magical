package com.sofar.snapu.feature.daq

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.sofar.utility.FileUtil
import java.io.File

object CaptureFileUtil {

  private const val PHOTO_DIR: String = "photo"
  private const val VIDEO_DIR: String = "video"

  fun getPhotoDir(context: Context): String {
    val dir = File(FileUtil.getFileDir(context), PHOTO_DIR)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir.absolutePath
  }

  fun getVideoDir(context: Context): String {
    val dir = File(FileUtil.getFileDir(context), VIDEO_DIR)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir.absolutePath
  }

  fun getPhotoUri(context: Context, name: String): Uri {
    val file = File(getPhotoDir(context), name)
    return getUriForFile(context, file)
  }

  fun getVideoUri(context: Context, name: String): Uri {
    val file = File(getVideoDir(context), name)
    return getUriForFile(context, file)
  }

  fun getUriForFile(context: Context, file: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file)
    } else {
      Uri.fromFile(file)
    }
  }
}