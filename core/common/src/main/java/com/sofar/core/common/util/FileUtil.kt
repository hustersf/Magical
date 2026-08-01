package com.sofar.core.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.annotation.WorkerThread
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Objects

/**
 * 通用文件系统管理与高性能 I/O 操作工具类。
 *
 * 本类基于 Java NIO 通道 (FileChannel) 技术实现，提供基础的文件与目录复制、删除及创建能力。
 * 内部全面引入自动化生命周期管理，确保所有流与通道资源的绝对安全释放。
 */
object FileUtil {
  private const val TAG = "FileUtil"

  /**
   * 流读写及通道传输的默认缓冲区大小（8KB），在绝大多数 Android 设备上具备最优的读写性能。
   */
  private const val BUFFER_SIZE = 8192

  /**
   * 将源文件拷贝到目标文件。
   *
   * @param srcFile  源文件，必须存在且不能是目录
   * @param destFile 目标文件
   * @throws IOException 如果执行拷贝期间发生物理 I/O 错误，或文件大小校验失败
   */
  @JvmStatic
  @WorkerThread
  @Throws(IOException::class)
  fun copyFile(srcFile: File, destFile: File) {
    requireExists(srcFile, "srcFile")
    requireFile(srcFile, "srcFile")
    createParentDirectories(destFile)
    requireFileIfExists(destFile, "destFile")
    if (destFile.exists()) {
      requireCanWrite(destFile, "destFile")
    }
    doCopy(srcFile.absolutePath, destFile.absolutePath)
    requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length())
  }

  /**
   * 底层基于 NIO 通道技术的高性能复制核心实现。
   */
  private fun doCopy(srcPath: String, destPath: String) {
    try {
      FileInputStream(srcPath).use { fis ->
        FileOutputStream(destPath).use { fos ->
          fis.channel.use { finC ->
            fos.channel.use { foutC ->
              val buffer = ByteBuffer.allocate(BUFFER_SIZE)
              while (finC.read(buffer) != -1) {
                buffer.flip()
                foutC.write(buffer)
                buffer.clear()
              }
            }
          }
        }
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  /**
   * 递归拷贝源目录下的所有子目录和文件到目标目录。
   *
   * @param srcDir  源目录，必须存在且必须是目录
   * @param destDir 目标目录
   * @throws IOException 如果执行拷贝期间发生物理 I/O 错误
   */
  @JvmStatic
  @WorkerThread
  @Throws(IOException::class)
  fun copyDirectory(srcDir: File, destDir: File) {
    requireExists(srcDir, "srcDir")
    requireDirectory(srcDir, "srcDir")

    var exclusionList: MutableList<String>? = null
    val srcDirCanonicalPath = srcDir.canonicalPath
    val destDirCanonicalPath = destDir.canonicalPath
    if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
      val srcFiles = srcDir.listFiles()
      if (srcFiles != null && srcFiles.isNotEmpty()) {
        exclusionList = ArrayList(srcFiles.size)
        for (srcFile in srcFiles) {
          val copiedFile = File(destDir, srcFile.name)
          exclusionList.add(copiedFile.canonicalPath)
        }
      }
    }
    doCopyDirectory(srcDir, destDir, exclusionList)
  }

  @Throws(IOException::class)
  private fun doCopyDirectory(srcDir: File, destDir: File, exclusionList: List<String>?) {
    val srcFiles = srcDir.listFiles() ?: return
    requireDirectoryIfExists(destDir, "destDir")
    mkdirs(destDir)
    requireCanWrite(destDir, "destDir")
    for (srcFile in srcFiles) {
      val dstFile = File(destDir, srcFile.name)
      if (exclusionList == null || !exclusionList.contains(srcFile.canonicalPath)) {
        if (srcFile.isDirectory) {
          doCopyDirectory(srcFile, dstFile, exclusionList)
        } else {
          copyFile(srcFile, dstFile)
        }
      }
    }
  }

  /**
   * 静默删除指定的文件或目录，即使发生异常或目标不存在也绝不向外抛出。
   *
   * @param file 待删除的文件或目录，支持传入 null
   * @return 如果删除成功返回 true，否则返回 false
   */
  @JvmStatic
  fun deleteQuietly(file: File?): Boolean {
    if (file == null) return false
    try {
      if (file.isDirectory) {
        cleanDirectory(file)
      }
    } catch (ignored: Exception) {
    }
    return try {
      file.delete()
    } catch (ignored: Exception) {
      false
    }
  }

  /**
   * 删除指定的目录及其内部包含的所有子文件与目录。
   *
   * @param directory 待删除的目录
   * @throws IOException 如果中途删除任一文件失败或无权限
   */
  @JvmStatic
  @Throws(IOException::class)
  fun deleteDirectory(directory: File) {
    Objects.requireNonNull(directory, "directory")
    if (!directory.exists()) return
    cleanDirectory(directory)
    directory.delete()
  }

  /**
   * 清空指定目录下的所有内容，但保留当前目录自身。
   *
   * @param directory 待清空的目录
   * @throws IOException 如果清空中途有任何子文件删除失败
   */
  @JvmStatic
  @Throws(IOException::class)
  fun cleanDirectory(directory: File) {
    val files = directory.listFiles() ?: return
    val causeList: MutableList<Exception> = ArrayList()
    for (file in files) {
      try {
        forceDelete(file)
      } catch (ioe: IOException) {
        causeList.add(ioe)
      }
    }
    if (causeList.isNotEmpty()) {
      throw IOException(causeList.toString())
    }
  }

  /**
   * 强行删除指定的文件或目录。如果目标是目录，将执行级联删除。
   *
   * @param file 待强删的文件或目录
   * @throws IOException 如果文件不存在或物理删除失败
   */
  @JvmStatic
  @Throws(IOException::class)
  fun forceDelete(file: File) {
    if (file.isDirectory) {
      deleteDirectory(file)
    } else {
      val filePresent = file.exists()
      if (!file.delete()) {
        if (!filePresent) {
          throw FileNotFoundException("File does not exist: $file")
        }
        throw IOException("Unable to delete file: $file")
      }
    }
  }

  /**
   * 确保创建指定的目录。若父目录不存在将一并创建。
   */
  @JvmStatic
  @Throws(IOException::class)
  fun forceMkdir(directory: File) {
    mkdirs(directory)
  }

  /**
   * 确保创建指定文件的父级完整目录树。
   *
   * @param file 目标文件
   * @return 创建成功后的父目录对象
   */
  @JvmStatic
  @Throws(IOException::class)
  fun createParentDirectories(file: File): File? {
    return mkdirs(getParentFile(file))
  }

  @Throws(IOException::class)
  private fun mkdirs(directory: File?): File? {
    if (directory != null && !directory.mkdirs() && !directory.isDirectory) {
      throw IOException("Cannot create directory '" + directory + "'.")
    }
    return directory
  }

  private fun getParentFile(file: File?): File? {
    return file?.parentFile
  }

  private fun requireFileIfExists(file: File, name: String): File {
    Objects.requireNonNull(file, name)
    return if (file.exists()) requireFile(file, name) else file
  }

  private fun requireDirectoryIfExists(directory: File, name: String): File {
    Objects.requireNonNull(directory, name)
    if (directory.exists()) {
      requireDirectory(directory, name)
    }
    return directory
  }

  private fun requireExists(file: File, fileParamName: String): File {
    Objects.requireNonNull(file, fileParamName)
    require(file.exists()) {
      "File system element for parameter '$fileParamName' does not exist: '$file'"
    }
    return file
  }

  private fun requireFile(file: File, name: String): File {
    Objects.requireNonNull(file, name)
    require(file.isFile) { "Parameter '$name' is not a file: $file" }
    return file
  }

  private fun requireDirectory(directory: File, name: String): File {
    Objects.requireNonNull(directory, name)
    require(directory.isDirectory) { "Parameter '$name' is not a directory: '$directory'" }
    return directory
  }

  private fun requireCanWrite(file: File, name: String) {
    Objects.requireNonNull(file, "file")
    require(file.canWrite()) { "File parameter '$name is not writable: '$file'" }
  }

  @Throws(IOException::class)
  private fun requireEqualSizes(srcFile: File, destFile: File, srcLen: Long, dstLen: Long) {
    if (srcLen != dstLen) {
      throw IOException("Failed to copy full contents from '$srcFile' to '$destFile' Expected length: $srcLen Actual: $dstLen")
    }
  }

  /**
   * 判断当前外部存储（SD卡）是否挂载并可执行读写。
   *
   * @return 如果外部存储已挂载且可读写返回 true，否则返回 false
   */
  @JvmStatic
  fun isSDCardEnable(): Boolean {
    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
  }

  /**
   * 获取应用专属的缓存目录。
   *
   * 优先返回外部存储沙盒路径（SD卡），若不可用则自动返回应用内部私有存储路径。
   *
   * @param context 上下文环境
   * @return 应用专属的缓存目录文件对象，可能为 null
   */
  @JvmStatic
  fun getCacheDir(context: Context): File? {
    return if (isSDCardEnable()) {
      context.externalCacheDir
    } else {
      context.cacheDir
    }
  }

  /**
   * 获取应用专属的文件物理持久化目录。
   *
   * 优先返回外部存储沙盒路径（SD卡），若不可用则自动返回应用内部私有存储路径。
   *
   * @param context 上下文环境
   * @return 应用专属的文件持久化目录文件对象，可能为 null
   */
  @JvmStatic
  fun getFileDir(context: Context): File? {
    return if (isSDCardEnable()) {
      context.getExternalFilesDir(null)
    } else {
      context.filesDir
    }
  }

  /**
   * 将输入流的数据源持续写入到指定的目标文件中。
   *
   * @param dstFile    目标写入文件
   * @param dataSource 数据源输入流，执行完毕后将自动安全关闭
   */
  @JvmStatic
  @WorkerThread
  fun writeToFile(dstFile: File, dataSource: InputStream) {
    try {
      if (!dstFile.exists()) {
        dstFile.createNewFile()
      }
      // 使用 use 嵌套自动接管输入流与输出流的生命周期，中途抛出异常亦能自动关闭
      dataSource.use { input ->
        FileOutputStream(dstFile).use { fos ->
          val buffer = ByteArray(BUFFER_SIZE)
          var len: Int
          while (input.read(buffer).also { len = it } != -1) {
            fos.write(buffer, 0, len)
          }
          fos.flush()
        }
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  /**
   * 读取指定的文本文件内容。
   *
   * @param file 待读取的文本文件对象
   * @return 文件的完整文本字符串内容，若读取失败则返回空字符串
   */
  @JvmStatic
  @WorkerThread
  fun getTextFromFile(file: File): String {
    if (!file.exists() || !file.isFile) return ""
    val sb = StringBuilder()
    try {
      // 自动托管 Reader 和 BufferedReader，执行完毕后自动安全闭合
      FileReader(file).use { reader ->
        BufferedReader(reader).use { br ->
          var line: String?
          while (br.readLine().also { line = it } != null) {
            sb.append(line).append("\n")
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return sb.toString()
  }

  /**
   * 从应用 Assets 资产目录下读取指定的文本文件。
   *
   * @param context  上下文环境
   * @param fileName Assets 下的文件相对路径名
   * @return 资产文件内的完整字符串，若读取失败返回空字符串
   */
  @JvmStatic
  fun getTextFromAssets(context: Context, fileName: String): String {
    return try {
      context.assets.open(fileName).use { isStream ->
        val buffer = ByteArray(isStream.available())
        isStream.read(buffer)
        String(buffer, Charsets.UTF_8)
      }
    } catch (e: IOException) {
      e.printStackTrace()
      ""
    }
  }

  @JvmStatic
  fun getImageFromAssetsFile(context: Context, fileName: String): Bitmap? {
    val am = context.resources.assets
    return try {
      // 使用 use 自动接管 Asset 资产输入流的生命周期，中途发生任何崩溃均能 100% 自动安全释放
      am.open(fileName).use { isStream ->
        BitmapFactory.decodeStream(isStream)
      }
    } catch (e: IOException) {
      e.printStackTrace()
      null
    }
  }

}
