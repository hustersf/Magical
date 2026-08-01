package com.sofar.download

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

object ZipUtil {

  /**
   * 解压 zip 文件到目标目录
   *
   * @param zipFile      输入的压缩包文件
   * @param destDir      解压的目标目录（默认解压到压缩包同级目录）
   * @param deleteSource 解压成功后是否删除源压缩包
   */
  fun unzip(
    zipFile: File,
    destDir: File = zipFile.absoluteFile.parentFile?.absoluteFile ?: File("."),
    deleteSource: Boolean = false
  ) {
    if (!destDir.exists()) destDir.mkdirs()
    val unzipBuffer = ByteArray(DEFAULT_BUFFER_SIZE)

    ZipFile(zipFile).use { zip ->
      val entries = zip.entries()

      while (entries.hasMoreElements()) {
        val entry = entries.nextElement()

        // 统一跨平台路径分隔符
        val normalizedName = entry.name.replace('\\', '/')
        if (normalizedName.isBlank()) continue

        // 安全校验：防止路径穿越攻击
        val targetFile = resolveTargetFile(destDir, normalizedName)
        if (entry.isDirectory) {
          targetFile.mkdirs()
          continue
        }

        // 确保文件的父目录已创建
        targetFile.parentFile?.mkdirs()

        // 使用临时文件写入，避免残留损坏文件
        val tmpFile = File(targetFile.parent, "${targetFile.name}.tmp")
        try {
          zip.getInputStream(entry).use { input ->
            FileOutputStream(tmpFile).use { output ->
              var len: Int
              while (input.read(unzipBuffer).also { len = it } > 0) {
                output.write(unzipBuffer, 0, len)
              }
            }
          }
          // 写入成功后原子更名
          if (tmpFile.exists()) {
            if (targetFile.exists()) targetFile.delete()
            if (!tmpFile.renameTo(targetFile)) {
              throw IOException("重命名临时文件失败: ${tmpFile.absolutePath}")
            }
          }
        } catch (e: Exception) {
          if (tmpFile.exists()) tmpFile.delete()
          throw e
        }
      }
    }

    if (deleteSource) {
      zipFile.delete()
    }
  }

  /**
   * 路径安全校验
   */
  private fun resolveTargetFile(destDir: File, relativeName: String): File {
    val candidate = File(destDir, relativeName)
    val destPath = destDir.canonicalFile.toPath()
    val candidatePath = candidate.canonicalFile.toPath()

    if (!candidatePath.startsWith(destPath)) {
      throw IOException("检测到恶意路径穿越攻击: $relativeName")
    }

    return candidate
  }
}