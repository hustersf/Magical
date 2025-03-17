package com.sofar.snapu.feature.daq.model

annotation class ListMode {
  companion object {
    const val LIST_ALL = 0
    const val LIST_UPLOAD_PENDING = 1
    const val LIST_UPLOADED = 2
  }
}
