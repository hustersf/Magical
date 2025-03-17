package com.sofar.snapu.feature.daq

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.sofar.utility.FileUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object CaptureFileUtil {

  fun mkdirs(context: Context, path: String): String {
    val dir = File(FileUtil.getFileDir(context), path)
    if (!dir.exists()) {
      dir.mkdirs()
    }
    return dir.absolutePath
  }

  fun getUriForFile(context: Context, file: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file)
    } else {
      Uri.fromFile(file)
    }
  }

  fun writeText(file: File, content: String) {
    try {
      FileOutputStream(file).use { stream ->
        stream.write(content.toByteArray())
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  fun readText(file: File): String {
    return if (file.exists()) {
      try {
        FileInputStream(file).bufferedReader().use { reader ->
          reader.readText()
        }
      } catch (e: IOException) {
        ""
      }
    } else {
      ""
    }
  }
}