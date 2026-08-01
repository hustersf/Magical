package com.sofar.core.common.extension

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

private const val TAG = "CryptExt"
private val HEX_CHARS =
  charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**
 * 字符串快速计算成常规 MD5 小写
 * 调用示例: val hash = "sofar_app".toMD5()
 */
fun String.toMD5(): String? {
  if (this.isEmpty()) return null
  return try {
    val md = MessageDigest.getInstance("MD5")
    md.update(this.toByteArray())
    val bytes = md.digest()
    val result = CharArray(bytes.size * 2)
    var k = 0
    for (b in bytes) {
      result[k++] = HEX_CHARS[(b.toInt() ushr 4) and 0xf]
      result[k++] = HEX_CHARS[b.toInt() and 0xf]
    }
    String(result)
  } catch (e: Exception) {
    null
  }
}

/**
 * 字符串快速计算 SHA-1
 * 调用示例: val sha1Str = "sofar_app".toSHA1()
 */
fun String.toSHA1(): String {
  if (this.isEmpty()) return ""
  return try {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(this.toByteArray())
    val bytes = md.digest()
    val buff = CharArray(bytes.size * 2)
    var c = 0
    for (b in bytes) {
      val v = b.toInt() and 0xff
      buff[c++] = HEX_CHARS[v shr 4]
      buff[c++] = HEX_CHARS[v and 0x0f]
    }
    String(buff)
  } catch (e: Exception) {
    ""
  }
}

/**
 * 本地文件高安全一键直取 MD5（支持大文件，自动高防流销毁）
 * 调用示例: val fileHash = File("/sdcard/config.json").getFileMD5()
 */
fun File.getFileMD5(): String? {
  if (!this.exists()) return null
  return try {
    val md = MessageDigest.getInstance("MD5")
    val buffer = ByteArray(4096)

    // 💡 核心安全科技：使用 Kotlin 官方标准库自带的 .use {}
    FileInputStream(this).use { inStream ->
      var readCount: Int
      while (inStream.read(buffer).also { readCount = it } != -1) {
        md.update(buffer, 0, readCount)
      }
    }

    val bytes = md.digest()
    val result = CharArray(bytes.size * 2)
    var k = 0
    for (b in bytes) {
      result[k++] = HEX_CHARS[(b.toInt() ushr 4) and 0xf]
      result[k++] = HEX_CHARS[b.toInt() and 0xf]
    }
    String(result)
  } catch (e: Exception) {
    Log.e(TAG, "calculate file md5 failed: ${this.absolutePath}", e)
    null
  }
}
