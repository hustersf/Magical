package com.sofar.core.common.util

import java.math.BigDecimal
import java.math.RoundingMode

object ByteConvertUtil {
  private const val UNIT = 1024L

  /**
   * 将字节数格式化为人类可读的字符串（自动适配 B, KB, MB, GB, TB）
   * @param bytes 字节数
   * @param decimalPlaces 保留的小数位数，默认为 2
   */
  fun formatBytes(bytes: Long, decimalPlaces: Int = 2): String {
    if (bytes < UNIT) return "$bytes B"

    // 计算单位层级（0=B, 1=KB, 2=MB, 3=GB, 4=TB）
    val exp = (Math.log(bytes.toDouble()) / Math.log(UNIT.toDouble())).toInt()
    // 防止 exp 超出预定义的单位数组范围
    val unitIndex = minOf(exp, 4)

    val value = bytes / Math.pow(UNIT.toDouble(), unitIndex.toDouble())
    val units = arrayOf("B", "KB", "MB", "GB", "TB")

    return String.format("%.${decimalPlaces}f %s", value, units[unitIndex])
  }

  /**
   * 将字节精确转换为 GB（返回 Double 数值）
   * @param bytes 字节数
   * @param decimalPlaces 保留的小数位数
   */
  fun bytesToGB(bytes: Long, decimalPlaces: Int = 2): Double {
    if (bytes <= 0) return 0.0
    val gb = bytes.toDouble() / (UNIT * UNIT * UNIT)
    return BigDecimal(gb).setScale(decimalPlaces, RoundingMode.HALF_UP).toDouble()
  }

  /**
   * 将 GB 精确转换为字节
   */
  fun gbToBytes(gb: Double): Long {
    return (gb * UNIT * UNIT * UNIT).toLong()
  }
}