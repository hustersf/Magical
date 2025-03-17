package com.sofar.snapu.feature.daq

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Pair
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtil {

  fun resizeBitmap(targetedSize: Pair<Int, Int>, oriBitmap: Bitmap): Bitmap {
    // Determine how much to scale down the image
    val scaleFactor =
      Math.max(
        oriBitmap.width.toFloat() / targetedSize.first.toFloat(),
        oriBitmap.height.toFloat() / targetedSize.second.toFloat()
      )
    return Bitmap.createScaledBitmap(
      oriBitmap,
      (oriBitmap.width / scaleFactor).toInt(),
      (oriBitmap.height / scaleFactor).toInt(),
      true
    )
  }

  fun getBitmapFromFilePth(imagePath: String): Bitmap? {
    val decodedBitmap = BitmapFactory.decodeFile(imagePath) ?: return null
    val orientation: Int = getExifOrientationTag(imagePath)

    var rotationDegrees = 0
    var flipX = false
    var flipY = false
    // See e.g. https://magnushoff.com/articles/jpeg-orientation/ for a detailed explanation on each
    // orientation.
    when (orientation) {
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
      ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
      ExifInterface.ORIENTATION_TRANSPOSE -> {
        rotationDegrees = 90
        flipX = true
      }

      ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
      ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
      ExifInterface.ORIENTATION_TRANSVERSE -> {
        rotationDegrees = -90
        flipX = true
      }

      ExifInterface.ORIENTATION_UNDEFINED, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL -> {}
      else -> {}
    }

    return rotateBitmap(
      decodedBitmap,
      rotationDegrees,
      flipX,
      flipY
    )
  }

  private fun getExifOrientationTag(imagePath: String): Int {
    val exif = ExifInterface(imagePath)
    return exif.getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL
    )
  }


  private fun rotateBitmap(
    bitmap: Bitmap,
    rotationDegrees: Int,
    flipX: Boolean,
    flipY: Boolean
  ): Bitmap {
    val matrix = Matrix()

    // Rotate the image back to straight.
    matrix.postRotate(rotationDegrees.toFloat())

    // Mirror the image along the X or Y axis.
    matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
    val rotatedBitmap =
      Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    // Recycle the old bitmap if it has changed.
    if (rotatedBitmap != bitmap) {
      bitmap.recycle()
    }
    return rotatedBitmap
  }

  fun saveToFile(bitmap: Bitmap, file: File): Boolean {
    try {
      FileOutputStream(file).use { stream ->
        if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
          return true
        }
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return false
  }
}