package com.k2fsa.sherpa.onnx

/**
 * Sherpa Native 库加载入口。
 *
 * 整个应用只加载一次，
 * 避免每个 Wrapper 类重复调用 System.loadLibrary()。
 */
internal object SherpaNativeLoader {

  init {
    System.loadLibrary("sherpa-onnx-jni")
  }
}