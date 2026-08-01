package com.sofar.core.common.extension

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 缓存最常用的默认日期时间格式化器
 * DateTimeFormatter 是原生线程安全的，可直接作为全局常量复用，彻底终结了 SimpleDateFormat 的闪退隐患
 */
private val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

/**
 * 将 Long 类型毫秒时间戳转换为指定格式的字符串
 * 采用原生线程安全的 java.time 框架，杜绝内存抖动与多线程闪退风险
 *
 * 示例：val timeStr = 1718944512000L.toDateTimeString()
 */
@JvmOverloads
fun Long.toDateTimeString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
  // 将毫秒时间戳转换为机器时间线上的瞬时点
  val instant = Instant.ofEpochMilli(this)
  // 结合系统当前时区，转换为本地日期时间
  val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

  return if (pattern == "yyyy-MM-dd HH:mm:ss") {
    DEFAULT_FORMATTER.format(localDateTime)
  } else {
    DateTimeFormatter.ofPattern(pattern).format(localDateTime)
  }
}

/**
 * 获取当前系统的毫秒时间戳（作为全局核心高频入口，表达最直观）
 *
 * 示例：val now = currentTimeInLong()
 */
fun currentTimeInLong(): Long = System.currentTimeMillis()

/**
 * 获取当前系统时间的标准格式字符串
 *
 * 示例：val nowStr = currentTimeInString()
 */
fun currentTimeInString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
  return System.currentTimeMillis().toDateTimeString(pattern)
}

/**
 * 计算给定毫秒时间戳在 N 天前或 N 天后的本地日期时间对象
 * java.time.LocalDateTime 内部对大小月、闰年进行了最严密的防错计算
 *
 * 示例：val futureDate = System.currentTimeMillis().addDays(5)
 */
fun Long.addDays(days: Int): LocalDateTime {
  val instant = Instant.ofEpochMilli(this)
  val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
  // 传递正数代表向后加天数，负数代表向前减天数，自动处理跨月跨年
  return localDateTime.plusDays(days.toLong())
}
