package com.sofar.core.download

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipUtil {

  fun unzip(zipFile: File, destDir: File, deleteSource: Boolean = false) {
    if (!destDir.exists()) destDir.mkdirs()
    val unzipBuffer = ByteArray(4096)
    ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipIn ->
      var zipEntry: ZipEntry? = zipIn.nextEntry
      while (zipEntry != null) {
        val filePath = destDir.absolutePath + File.separator + zipEntry.name
        if (!zipEntry.isDirectory) {
          FileOutputStream(filePath).use { bos ->
            var len: Int
            while (zipIn.read(unzipBuffer).also { len = it } > 0) {
              bos.write(unzipBuffer, 0, len)
            }
          }
        } else {
          File(filePath).mkdirs()
        }
        zipIn.closeEntry()
        zipEntry = zipIn.nextEntry
      }
    }

    if (deleteSource) {
      zipFile.delete()
    }
  }
}
