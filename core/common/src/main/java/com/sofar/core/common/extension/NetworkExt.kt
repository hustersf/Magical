package com.sofar.core.common.extension

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/**
 * 判定当前网络是否连接且可用。
 *
 * 在低版本系统使用兼容型 NetworkInfo；在高版本系统利用现代 NetworkCapabilities
 * 进行多重互联网契约校验（NET_CAPABILITY_INTERNET 与 VALIDATED 真实触网校验）。
 */
fun Context.isNetworkAvailable(): Boolean {
  val manager =
    this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    capabilities != null &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  } else {
    @Suppress("DEPRECATION")
    manager.activeNetworkInfo?.isAvailable ?: false
  }
}

/**
 * 判定当前是否正在使用移动蜂窝网络（数据流量）。
 */
fun Context.isMobileAvailable(): Boolean {
  val manager =
    this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    capabilities != null &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
  } else {
    @Suppress("DEPRECATION")
    val networkInfo = manager.activeNetworkInfo
    networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isAvailable
  }
}

/**
 * 判定当前是否正在使用 Wi-Fi 无线网络。
 */
fun Context.isWifiAvailable(): Boolean {
  val manager =
    this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    capabilities != null &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  } else {
    @Suppress("DEPRECATION")
    val networkInfo = manager.activeNetworkInfo
    networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isAvailable
  }
}

/**
 * 现代化全兼容获取当前手机的局域网 IP 地址。
 *
 * 💡 升级：彻底抛弃了旧版依赖 WifiManager 的做法，无需任何定位或系统敏感隐私权限申请，
 * 直接通过底层套接字遍历物理网络接口（如 wlan0、eth0），100% 具备多线程与高版本系统抗崩溃防御力。
 */
fun Context.getIP(): String {
  try {
    val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
    for (netInterface in interfaces) {
      // 过滤未启用的网卡或本地回环测试接口 (loopback)
      if (!netInterface.isUp || netInterface.isLoopback) continue

      val addresses = Collections.list(netInterface.inetAddresses)
      for (address in addresses) {
        // 筛选纯净的 IPv4 地址，规避极其复杂的 IPv6 字符串
        if (!address.isLoopbackAddress && address is Inet4Address) {
          val hostAddress = address.hostAddress
          if (!hostAddress.isNullOrEmpty()) {
            return hostAddress
          }
        }
      }
    }
  } catch (e: Exception) {
    e.printStackTrace()
  }
  return "0.0.0.0"
}
